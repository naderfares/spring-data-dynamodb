/**
 * Spring Data DynamoDB <https://github.com/naderfares/spring-data-dynamodb>
 *
 * Copyright © 2018 (Nader Fares <naderfares@gmail.com>) All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.socialsignin.spring.data.dynamodb.domain.sample;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.socialsignin.spring.data.dynamodb.repository.ExpressionAttribute;
import org.socialsignin.spring.data.dynamodb.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface UserPaginationRepository
    extends PagingAndSortingRepository<User, String>, CrudRepository<User, String> {

  @EnableScan
  @EnableScanCount
  Page<User> findAllByName(String name, Pageable pageable);

  @EnableScan
  List<User> findAllByName(String name);

  @EnableScan
  List<User> findAll();

  @EnableScan
  void deleteAll();

  Page<User> findByPostCode(String postCode, Pageable pageable);

  @Query(limit = 10, filterExpression = "#date BETWEEN :startDate AND :endDate",
      expressionMappingNames = {@ExpressionAttribute(key = "#date", value = "joinDate")},
      expressionMappingValues = {
          @ExpressionAttribute(key = ":startDate", parameterName = "startDate"),
          @ExpressionAttribute(key = ":endDate", parameterName = "endDate")})
  Page<User> findByPostCode(String postCode, @Param("startDate") Date startDate,
      @Param("endDate") Date endDate, Pageable pageable);
}
