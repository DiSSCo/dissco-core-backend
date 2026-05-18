package eu.dissco.backend.properties;

import eu.dissco.backend.Profiles;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "s3")
@Profile(Profiles.MEDIA_DERIVATIVES)
public class S3Properties {

	@NotBlank
	private String accessKey;

	@NotBlank
	private String accessSecret;

	@NotBlank
	private String bucketName;

}
