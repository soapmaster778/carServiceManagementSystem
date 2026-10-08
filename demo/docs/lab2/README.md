# Laboratory work 2 - Unit testing evidence

## Verification command

```powershell
mvn verify
```

This command runs all tests, creates the JaCoCo report, and fails the build when total line coverage is below 60%.

Coverage report: `target/site/jacoco/index.html`.

## Mutation check

To prove that the test checks real behavior, `RepairOrderService.findById` was temporarily changed to return `null`. Only the relevant test was then executed:

```powershell
mvn -q "-Dtest=RepairOrderServiceTest#findById_existingId_returnsMappedDto" test
```

The test failed because it expected a mapped `RepairOrderDto` but received `null`. The captured console output is stored in `mutation-test-proof.txt`. The production implementation was restored immediately afterward, and the complete `mvn verify` build was run again.
