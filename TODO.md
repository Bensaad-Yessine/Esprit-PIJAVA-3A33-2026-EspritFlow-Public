# Groq AI Proposition Reunion Generator - Frontend Only

## Completed (0/7)

## Pending
- [ ] 1. Update pom.xml: Add httpclient5 + gson dependencies
- [ ] 2. Create src/main/java/piJava/utils/GroqService.java (API client, generate prop JSON -> entity)
- [x] 3. Update src/main/java/piJava/Controllers/frontoffice/group/GroupContentController.java: Add AI btn in createGroupCard actionRow, handleGenerateAI(GroupRecord)
- [x] 4. Test compilation: mvn clean compile (fixed errors)
- [ ] 5. Test runtime: Run app, front groups, click AI → prop created, verify DB/list refresh
- [ ] 6. Handle edge cases (API fail, invalid JSON, dupes)
- [ ] 7. Final verification + attempt_completion

**Notes**: Use groupRecord.id as idGroupeId. Prompt: "JSON meeting prop for [nom]/[projet]: [desc]". Model: llama3-8b-8192
