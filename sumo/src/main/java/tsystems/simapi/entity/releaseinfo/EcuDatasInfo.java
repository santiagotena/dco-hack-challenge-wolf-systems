package tsystems.simapi.entity.releaseinfo;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EcuDatasInfo {
    private String actualBatteryCapacity;
    private String maxTemperature;
    private String minTemperature;
}
