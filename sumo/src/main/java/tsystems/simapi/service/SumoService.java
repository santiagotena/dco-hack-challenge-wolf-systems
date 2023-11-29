package tsystems.simapi.service;

import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tsystems.simapi.component.SumoFileGateway;
import tsystems.simapi.entity.SumoXMLObject;
import tsystems.simapi.entity.releaseinfo.EcuDatas;
import tsystems.simapi.entity.releaseinfo.EcuDatasInfo;
import tsystems.simapi.entity.releaseinfo.FunctionInfo;
import tsystems.simapi.entity.releaseinfo.ReleaseInfo;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

import static java.lang.Math.exp;
import static java.lang.Math.pow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Slf4j
public class SumoService {

    @Autowired
    private final SumoFileGateway sumoFileGateway;
    private static final Logger LOGGER = LoggerFactory.getLogger(SumoService.class);

    private final Double defaultActivationEnergy = 60000d;
    private final Double gasConstant = 8.314d;
    private final Double normalTemperatureAbs = 293.15d;
    private final Double leadAcidBatteryCof = 0.4d;

    public SumoService(SumoFileGateway sumoFileGateway) {
        this.sumoFileGateway = sumoFileGateway;
    }

    public Boolean runSimulation() {
        List<Integer> exitCodes = new ArrayList<>();
        exitCodes.add(executeCommand(getSimulationCommand()));
        exitCodes.add(executeCommand(getCopyLogsCommand()));
        exitCodes.add(executeCommand(getBatteryPerformance()));

        if (exitCodes.stream().anyMatch(num -> num == -1)) {
            System.out.println("An error occurred while executing the scripts.");
            return false;
        } else if (exitCodes.stream().anyMatch(num -> num == 1)) {
            System.out.println("A command execution failed.");
            return false;
        }
        System.out.println("Commands executed successfully");
        return true;
    }

    public List<String> getSimulationCommand() {
        return List.of("/sumo/bin/sumo",
                "-c",
                "sumo_scenario/dua.actuated.sumocfg",
                "--fcd-output",
                "output-files/outputs/vehicle-trajectories.xml",
                "--emission-output",
                "output-files/outputs/emissions.xml");
    }

    public List<String> getCopyLogsCommand() {
        return List.of("cp",
                "sumo_scenario/Battery.out.xml",
                "sumo_scenario/dua.actuated.log",
                "sumo_scenario/dua.actuated.summary.xml",
                "sumo_scenario/dua.actuated.tripinfo.xml",
                "output-files/logs");
    }

    public List<String> getBatteryPerformance() {
        return List.of("python3",
                "visualization-scripts/batteryPerformance.py",
                "output-files/logs/Battery.out.xml",
                "--output",
                "output-files/graphs/BatteryPerformance.png"
        );
    }

    public static int executeCommand(List<String> command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(command);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0)
                exitCode = 1;
            return exitCode;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void configureSumo(ReleaseInfo releaseInfo) {
        try {
            SumoXMLObject routesConfig = sumoFileGateway.readDefaultConfig();
            routesConfig = changeConfigs(routesConfig, releaseInfo);
            sumoFileGateway.writeConfigs(routesConfig);
            log.info("Updated route config file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SumoXMLObject changeConfigs(SumoXMLObject routesConfig, ReleaseInfo releaseInfo) {
        int numberOfVehicles = releaseInfo.getFunctions().size();
        int maxIter = Math.min(numberOfVehicles, 3);
        log.info("release info is :\n" + releaseInfo.toString());
        for (int i = 0; i < maxIter; i++) {
            SumoXMLObject.Vehicle vehicleToUpdate = routesConfig.getVehicles().get(i);
            EcuDatasInfo ecuDatasInfo = releaseInfo
                    .getFunctions()
                    .get(i)
                    .getEcuDatas()
                    .get(0)
                    .getData();

            for (SumoXMLObject.Vehicle.Param param : vehicleToUpdate.getParams()) {
                if (param.getKey().equals("device.battery.chargeLevel")) {
                    param.setValue(ecuDatasInfo.getActualBatteryCapacity());
                    break;
                }
            }
        }
        return routesConfig;
    }

    EcuDatasInfo adjustEcuToTemperature(Double temperatureAbs, Double capacity, Double temperatureCoificient) {
        Double result = capacity * pow(temperatureAbs/normalTemperatureAbs, temperatureCoificient);
        return new EcuDatasInfo(String.valueOf(result), "20", "20");
    }

    FunctionInfo createNewFunction(FunctionInfo defaultFunction, String newNameExt, EcuDatasInfo newData){
        EcuDatas defaultEcu = defaultFunction.getEcuDatas().get(0);
        return FunctionInfo.builder().name(defaultFunction.getName() + newNameExt).ecuDatas(List.of(EcuDatas.builder()
                        .ecu(defaultEcu.getEcu())
                        .componentId(defaultEcu.getComponentId())
                        .componentName(defaultEcu.getComponentName())
                        .componentVersion(defaultEcu.getComponentVersion())
                        .hardwareVersion(defaultEcu.getHardwareVersion())
                        .status(defaultEcu.getStatus())
                        .lastChange(defaultEcu.getLastChange())
                        .data(newData)
                        .build()))
                .build();
    }

    public ReleaseInfo adjustBatteryToTemperature(ReleaseInfo defaultInfo) {
        EcuDatasInfo defaultEcu = defaultInfo.getFunctions().get(0).getEcuDatas().get(0).getData();

        Double minTemperatureAbs = Double.parseDouble(defaultEcu.getMinTemperature()) + 273.15;
        Double maxTemperatureAbs = Double.parseDouble(defaultEcu.getMaxTemperature()) + 273.15;
        Double defaultCapacity = Double.parseDouble(defaultEcu.getActualBatteryCapacity());

        EcuDatasInfo minInfo = adjustEcuToTemperature(minTemperatureAbs, defaultCapacity, leadAcidBatteryCof);
        EcuDatasInfo maxInfo = adjustEcuToTemperature(maxTemperatureAbs, defaultCapacity, leadAcidBatteryCof);

        defaultInfo.getFunctions().add(createNewFunction(defaultInfo.getFunctions().get(0), "COLD", minInfo));
        defaultInfo.getFunctions().add(createNewFunction(defaultInfo.getFunctions().get(0), "HOT", maxInfo));

        log.info("new info is : " + defaultInfo.toString());
        return defaultInfo;
    }

    public ResponseEntity<Map<String, List<Object>>> getPlotImages() {
        List<Object> images = new ArrayList<>();
        String[] imagePaths = {"output-files/graphs/Vehicle-Trajectories.png",
                "output-files/graphs/CO2_output.png"};

        for (String imagePath : imagePaths) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                try (InputStream inputStream = new FileInputStream(imageFile)) {
                    images.add(imageFile);
                } catch (IOException e) {
                    LOGGER.error("Error reading image: {}", e.getMessage());
                }
            } else {
                LOGGER.warn("Image file does not exist: {}", imageFile);
            }
        }

        Map<String, List<Object>> imageMap = new HashMap<>();
        imageMap.put("plotImages", images);

        return ResponseEntity.ok(imageMap);
    }
}
