## Approach and Solution
Solution detail stored in:
```/solution_description/Travel_System_Solution_Description.pdf```
## System requisites
````
- Java 11
- Maven
- OS: Window or Linux
````
## Project structure
````
src
|_main
    |_java
		|com.travel.system.trelleborg
			|_common   					 --> common resources using in project code base
			|_components				 --> spring components support services to do some businesses
			|_controller   				 --> define restful controllers
			|_dto						 --> define Data transfer objects
			|_exceptions				 --> define exception return object and global exception handlers
			|_service					 --> define business service
			|TrelleborgApplication.java  --> main spring boot run file
````
## Run application
Goto folder: /trelleborg-travel-system

run:
    mvn clean package

run:
    mvn spring-boot:run
## Curl
````
curl --location --request GET 'http://localhost:8080/travel-system/api/trips-data' \
--form 'file=@"/path/to/file"'
````
## Test data
````
Stored in: src\main\resources\csv_test_data

case 1: "happycase_enought_data_on_off_samebus.csv"
    happy cases with 6 valid touch pair (3 touch on/ 3 touch off)

case 2: "one_incompleted_and_one_unprocessable.csv"
    8 touch record with 6 valid touch on/off (complete), 1 touch on without touch off (incompleted), 1 touch off without touch on (unprocessable)

case 3: "complext_cases.csv"
    10 touch record with:
    3 empty/null fields, 1 no touch on (4 unprocessable)
    3 completed, 2 incompleted, 1 cancelled (6 trips)
 
case 4: "10_valid_touch_3_days.csv"
    18 valid touch record in 3 days (9 complete)
  
case 5: "touch_data_difference_company_busId.csv"
   touch data with valid pairs of ON/OFF but difference companyID/busId (no valid touch on off data)
case 6: "200_touch_records.csv"
    touch data with 200 rows touch ON/OFF of PANs
````

