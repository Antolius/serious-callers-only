package hr.from.josipantolis.seriouscallersonly.app

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = ["sco.scripts.dir=src/test/resources/scripts"]
)
class ScriptLoadingTest(
    @Autowired val rest: TestRestTemplate,
    @Value("\${local.management.port}") val managementPort: Int
) {

    @Test
    fun `Should load scripts on startup`() {
        // given, when
        val actualRes = rest.getForEntity(
            "http://localhost:$managementPort/actuator/bot",
            String::class.java
        )

        // then
        then(actualRes.statusCode).isEqualTo(HttpStatus.OK)
        then(actualRes.body).isEqualTo("""{"channelProtocols":{},"commandProtocols":["/play"],"onBotJoinChannel":false,"onHomeTabVisit":false,"onPrivateMessage":false}""")
    }

}