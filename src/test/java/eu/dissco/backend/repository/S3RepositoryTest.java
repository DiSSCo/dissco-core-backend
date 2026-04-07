package eu.dissco.backend.repository;

import static eu.dissco.backend.TestUtils.SUFFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.ProcessingFailedException;
import eu.dissco.backend.properties.S3Properties;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class S3RepositoryTest {

	@Mock
	private S3AsyncClient s3AsyncClient;

	@Mock
	private S3Properties s3Properties;

	private S3Repository s3Repository;

	public static Stream<Arguments> provideExceptions() {
		return Stream.of(Arguments.of(404, NotFoundException.class),
				Arguments.of(500, ProcessingFailedException.class));
	}

	@BeforeEach
	void setup() {
		this.s3Repository = new S3Repository(s3AsyncClient, s3Properties);
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void testRetrieveMediaFromStorage(boolean isThumbnail) throws NotFoundException, ProcessingFailedException {
		// Given
		var bytea = "test-image".getBytes();
		var bucketName = "test-bucket";
		var fileName = SUFFIX + "/" + SUFFIX + "-" + (isThumbnail ? "thumbnail" : "derivative") + ".jpeg";
		given(s3Properties.getBucketName()).willReturn(bucketName);
		var getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(fileName).build();
		given(s3AsyncClient.getObject(eq(getObjectRequest), any(AsyncResponseTransformer.class)))
			.willReturn(CompletableFuture.completedFuture(ResponseBytes.fromByteArray(Object.class, bytea)));

		// When
		var result = s3Repository.retrieveMediaFromStorage(SUFFIX, isThumbnail);

		// Then
		assertThat(result).isEqualTo(bytea);
	}

	@ParameterizedTest
	@MethodSource("provideExceptions")
	void testRetrieveMediaFromStorageException(int statusCode, Class<Throwable> exceptionClass) {
		// Given
		var bucketName = "test-bucket";
		given(s3Properties.getBucketName()).willReturn(bucketName);
		var getObjectRequest = GetObjectRequest.builder()
			.bucket(bucketName)
			.key(SUFFIX + "/" + SUFFIX + "-derivative.jpeg")
			.build();
		given(s3AsyncClient.getObject(eq(getObjectRequest), any(AsyncResponseTransformer.class)))
			.willReturn(CompletableFuture.failedFuture(S3Exception.builder().statusCode(statusCode).build()));

		// When / Then
		assertThrows(exceptionClass, () -> s3Repository.retrieveMediaFromStorage(SUFFIX, false));
	}

}
