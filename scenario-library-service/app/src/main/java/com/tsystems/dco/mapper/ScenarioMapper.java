/*
 *   ========================================================================
 *  SDV Developer Console
 *
 *   Copyright (C) 2022 - 2023 T-Systems International GmbH
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 *
 *   ========================================================================
 */

package com.tsystems.dco.mapper;

import com.tsystems.dco.file.entity.FileEntity;
import com.tsystems.dco.model.FileData;
import com.tsystems.dco.model.Scenario;
import com.tsystems.dco.model.ScenarioInput;
import com.tsystems.dco.scenario.entity.ScenarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper
public interface ScenarioMapper {

  ScenarioMapper INSTANCE = Mappers.getMapper(ScenarioMapper.class);

  ScenarioEntity toEntity(ScenarioInput scenarioInput);

  List<Scenario> toModel(List<ScenarioEntity> scenarioEntities);

  Scenario toModel(ScenarioEntity scenarioEntity);

  FileEntity toEntity(FileData fileData);

}
