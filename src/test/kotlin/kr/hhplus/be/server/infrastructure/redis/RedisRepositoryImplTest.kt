package kr.hhplus.be.server.infrastructure.redis

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.data.redis.core.script.RedisScript
import java.time.Duration

class RedisRepositoryImplTest {
    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var objectMapper: ObjectMapper
    private lateinit var redisRepository: RedisRepositoryImpl

    @BeforeEach
    fun setup() {
        redisTemplate = mockk()
        objectMapper = ObjectMapper()
        redisRepository = RedisRepositoryImpl(redisTemplate, objectMapper)
    }

    @Nested
    inner class Exists {
        @Test
        @DisplayName("key가 존재하면 true를 반환")
        fun returnTrueWhenKeyExists() {
            // given
            every { redisTemplate.hasKey("existingKey") } returns true

            // when
            val result = redisRepository.exists("existingKey")

            // then
            assertThat(result).isTrue()
        }

        @Test
        @DisplayName("key가 존재하지 않으면 false 반환")
        fun returnFalseWhenKeyDoesNotExist() {
            // given
            every { redisTemplate.hasKey("missingKey") } returns false

            // when
            val result = redisRepository.exists("missingKey")

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class SetIfAbsent {
        @Test
        @DisplayName("정상적으로 true 반환")
        fun returnTrueWhenSet() {
            // given
            every {
                redisTemplate.opsForValue().setIfAbsent("key", "value", any())
            } returns true

            // when
            val result = redisRepository.setIfAbsent("key", "value", Duration.ofSeconds(10))

            // then
            assertThat(result).isTrue()
        }
    }

    @Nested
    inner class ExecuteWithLua {
        @Test
        @DisplayName("스크립트 실행 결과가 null이 아니면 해당 값을 반환")
        fun returnResultWhenScriptExecutesSuccessfully() {
            // given
            val scriptBody = "return 1"
            val redisScript = RedisScript.of(scriptBody, Long::class.java)
            val keys = listOf("key1")
            val args = listOf("arg1")

            every {
                redisTemplate.execute(redisScript, keys, *args.toTypedArray())
            } returns 1L

            // when
            val result: Long? = redisRepository.executeWithLua(redisScript, keys, args)

            // then
            assertThat(result).isEqualTo(1L)
        }

        @Test
        @DisplayName("스크립트 실행 결과가 null 이면 0L을 반환")
        fun returnZeroWhenScriptReturnsNull() {
            // given
            val scriptBody = "return nil"
            val redisScript = RedisScript.of(scriptBody, Long::class.java)
            val keys = listOf("key1")
            val args = listOf("arg1")

            every {
                redisTemplate.execute(redisScript, keys, *args.toTypedArray())
            } returns 0L

            // when
            val result: Long = redisRepository.executeWithLua(redisScript, keys, args) ?: 0L

            // then
            assertThat(result).isEqualTo(0L)
        }
    }

    @Nested
    inner class ReleaseWithLua {
        @Test
        @DisplayName("key가 일치할 때 삭제 성공")
        fun deleteIfMatch() {
            // given
            every {
                redisTemplate.execute(any<RedisScript<Long>>(), listOf("key"), "value")
            } returns 1L

            // when
            val result = redisRepository.releaseWithLua("key", "value")

            // then
            assertThat(result).isTrue()
        }
    }

    @Nested
    inner class Save {
        @Test
        @DisplayName("리스트를 JSON 으로 직렬화하여 저장 성공")
        fun saveListToRedis() {
            // given
            val ops = mockk<ValueOperations<String, String>>()
            every { redisTemplate.opsForValue() } returns ops
            every { ops.set(any(), any(), any<Duration>()) } just Runs
            val list = listOf("a", "b", "c")

            // when
            redisRepository.save("key", list, Duration.ofSeconds(60))

            // then
            verify { ops.set("key", objectMapper.writeValueAsString(list), Duration.ofSeconds(60)) }
        }
    }

    @Nested
    inner class FindList {
        @Test
        @DisplayName("저장된 JSON 문자열을 List로 역직렬화 및 조회")
        fun deserializeJsonToList() {
            // given
            val ops = mockk<ValueOperations<String, String>>()
            every { redisTemplate.opsForValue() } returns ops
            val json = objectMapper.writeValueAsString(listOf("x", "y"))
            every { ops.get("listKey") } returns json

            // when
            val result = redisRepository.findList("listKey", String::class.java)

            // then
            assertThat(result).containsExactly("x", "y")
        }
    }
}
