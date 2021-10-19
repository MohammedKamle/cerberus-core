/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.api.mapper.v001;

import org.cerberus.api.dto.v001.TestcaseStepActionControlDTOV001;
import org.cerberus.api.mapper.JSONArrayMapper;
import org.cerberus.api.mapper.TimestampMapper;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author mlombard
 */
@Mapper(componentModel = "spring", uses = {JSONArrayMapper.class, TimestampMapper.class})
public interface TestcaseStepActionControlMapperV001 {

    @Mapping(source = "control.test", target = "testFolderId")
    @Mapping(source = "control.testcase", target = "testcaseId")
    TestcaseStepActionControlDTOV001 toDTO(TestCaseStepActionControl control);

    @Mapping(source = "controlDTO.testFolderId", target = "test")
    @Mapping(source = "controlDTO.testcaseId", target = "testcase")
    TestCaseStepActionControl toEntity(TestcaseStepActionControlDTOV001 controlDTO);
}
