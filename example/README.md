# Serious Callers Only scripting example

After building the Serious Callers Only docker image run the example scripts with:

```bash
docker run -p 3000:3000 -p 3001:3001 -t \
--mount type=bind,source="$(pwd)"/src/main/kotlin,target=/scripts,readonly \
-e SCO_SLACK_BOT_TOKEN="your-bot-token" \
-e SCO_SLACK_SIGNING_SECRET="your-signing-secret" \
-e SCO_SCRIPTS_DIR="/scripts" \
hr.from.josipantolis/serious-callers-only
```
