package eu.dissco.backend.configuration;

import eu.dissco.backend.Profiles;
import eu.dissco.backend.repository.DigitalMediaRepository;
import eu.dissco.backend.repository.DigitalSpecimenRepository;
import eu.dissco.backend.repository.MongoRepository;
import eu.dissco.backend.repository.S3Repository;
import eu.dissco.backend.service.AnnotationService;
import eu.dissco.backend.service.DigitalMediaService;
import eu.dissco.backend.service.MachineAnnotationServiceService;
import eu.dissco.backend.service.MasJobRecordService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class DigitalMediaServiceConfiguration {

	@Bean
	@Primary
	public DigitalMediaService digitalMediaServiceMediaDefault(DigitalMediaRepository repository,
			AnnotationService annotationService, DigitalSpecimenRepository digitalSpecimenRepository,
			MachineAnnotationServiceService masService, MongoRepository mongoRepository, JsonMapper jsonMapper,
			MasJobRecordService masJobRecordService) {
		return new DigitalMediaService(repository, annotationService, digitalSpecimenRepository, masService,
				mongoRepository, jsonMapper, masJobRecordService, null);
	}

	@Bean
	@Profile(Profiles.MEDIA_DERIVATIVES)
	public DigitalMediaService digitalMediaServiceMediaDerivative(DigitalMediaRepository repository,
			AnnotationService annotationService, DigitalSpecimenRepository digitalSpecimenRepository,
			MachineAnnotationServiceService masService, MongoRepository mongoRepository, JsonMapper jsonMapper,
			MasJobRecordService masJobRecordService, S3Repository s3Repository) {
		return new DigitalMediaService(repository, annotationService, digitalSpecimenRepository, masService,
				mongoRepository, jsonMapper, masJobRecordService, s3Repository);
	}

}
