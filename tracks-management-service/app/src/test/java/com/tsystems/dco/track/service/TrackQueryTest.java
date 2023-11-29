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

package com.tsystems.dco.track.service;

import com.tsystems.dco.exception.BaseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrackQueryTest {


  @Test
  void getQueries() {
    var raw = "brand:vw,brand!bmw,brand~benz,brand$invalid";
    var query = new TrackQuery(raw);
    assertEquals(3, query.getQueries().size());
    var spec = query.toSpecification();
    assertNotNull(spec);
    try {
      new TrackQuery("brandvw").isValid().toSpecification();
    } catch (Exception e) {
      assertInstanceOf(BaseException.class, e);
    }
  }
}

