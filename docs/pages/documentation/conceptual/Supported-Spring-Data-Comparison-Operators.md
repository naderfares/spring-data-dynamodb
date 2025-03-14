---
title: Supported Spring Data Comparison Operators
parent: Conceptual
layout: page
---

Spring-data-dynamodb supports both findBy and queryBy repository methods in the same way:

1. It tries to build a DynamoDB QueryRequest, but is only able to do so if all properties in the method name are one of
   the following:

* Hash Key
* Range Key
* GSI Hash Key
* GSI Range Key

2. It will fall back on a Scan if the properties used do not fall within the list above. For scan to work you need to
   annotate either the class or method with the @EnableScan annotation.

## Comparison Operators

This is the list of supported comparison operators that spring-data-dynamodb supports:

 spring-data              | DynamoDB    | Notes/Example                                             
--------------------------|-------------|-----------------------------------------------------------
 IN                       | EQ          | (With OR concatenation)                                   
 CONTAINING               | CONTAINS    |
 STARTING_WITH            | BEGINS_WITH |
 BETWEEN                  | BETWEEN     |
 AFTER                    | GT          |
 GREATER_THAN             | GT          |
 BEFORE                   | LT          |
 LESS_THAN                | LT          |
 GREATER_THAN_EQUAL       | GE          |
 LESS_THAN_EQUAL          | LE          |
 IS_NULL                  | NULL        |
 IS_NOT_NULL              | NOT_NULL    |
 TRUE                     | EQ          |
 FALSE                    | EQ          |
 SIMPLE_PROPERTY          | EQ          | Special conditions if the property is a HashKey/RangeKey. 
 NEGATING_SIMPLE_PROPERTY | NE          |

The list above was created by referencing
this [method](https://github.com/derjust/spring-data-dynamodb/blob/master/src/main/java/org/socialsignin/spring/data/dynamodb/repository/query/AbstractDynamoDBQueryCreator.java#L70).

## Partial Supported

There is support for Sort, but it will only work if it is performed on a RangeKey.

## Unsupported operators

* Case insensitivity
* Top
* First
* Like
* Not like

## Notes

This page is not complete; please update with any findings.
