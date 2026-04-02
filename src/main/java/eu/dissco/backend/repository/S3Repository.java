package eu.dissco.backend.repository;

import eu.dissco.backend.exceptions.NotFoundException;
import eu.dissco.backend.exceptions.ProcessingFailedException;
import eu.dissco.backend.properties.S3Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Repository
@RequiredArgsConstructor
public class S3Repository {

	private final S3AsyncClient s3Client;

	private final S3Properties properties;

	public byte[] retrieveMediaFromStorage(String suffix, boolean isThumbnail)
			throws NotFoundException, ProcessingFailedException {
		var mediaType = isThumbnail ? "thumbnail" : "derivative";
		var request = GetObjectRequest.builder()
			.bucket(properties.getBucketName())
			.key(suffix + "/" + suffix + "-" + mediaType + ".jpeg")
			.build();
		try {
			return s3Client.getObject(request, AsyncResponseTransformer.toBytes())
				.get(5, TimeUnit.SECONDS)
				.asByteArray();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Thread interrupted when retrieving media derivative from s3", e);
			throw new ProcessingFailedException("Thread got interrupted while retrieving media derivative");
		}
		catch (ExecutionException e) {
			if (e.getCause() instanceof S3Exception s3Exception && s3Exception.statusCode() == 404) {
				throw new NotFoundException();
			}
			throw new ProcessingFailedException("Unable to retrieve media derivative from S3");
		}
		catch (TimeoutException e) {
			log.error("Timed out while retrieving media derivative from S3", e);
			throw new ProcessingFailedException("Timed out while retrieving media derivative from S3");
		}
	}

}
