package no.novari.fint.explorer

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "fint.poll.initial-delay=3600000",
        "fint.poll.fixed-delay=3600000",
    ],
)
class ApplicationTests {

    @Test
    fun contextLoads() {
    }
}
