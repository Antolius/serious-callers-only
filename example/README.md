# Serious Callers Only scripting example

First you'll need to build te framework itself. For that open a terminal window in the root directory of the project and run:
 ```shell script
./gradlew clean build publish docker
```

Afterwards move into this `example` directory:

```shell script
cd example/
```

And start up the Serious Callers Only docker image:

```shell script
docker run -p 3000:3000 -p 3001:3001 -t \
--mount type=bind,source="$(pwd)"/src/scripts,target=/scripts,readonly \
-e SCO_SLACK_BOT_TOKEN="your-bot-access-token" \
-e SCO_SLACK_SIGNING_SECRET="your-signing-secret" \
-e SCO_SCRIPTS_DIR="/scripts" \
hr.from.josipantolis/serious-callers-only
```
Just make sure to define the `SCO_SLACK_BOT_TOKEN` and `SCO_SLACK_SIGNING_SECRET` environment variables. FOr more info on setting up your Slack config check out the main project's README.md file.