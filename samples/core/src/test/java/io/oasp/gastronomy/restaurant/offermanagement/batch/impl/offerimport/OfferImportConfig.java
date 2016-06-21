package io.oasp.gastronomy.restaurant.offermanagement.batch.impl.offerimport;

import java.net.MalformedURLException;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.IncorrectTokenCountException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import io.oasp.gastronomy.restaurant.batch.common.CustomSkipListener;
import io.oasp.gastronomy.restaurant.offermanagement.batch.impl.offerimport.writer.OfferItemConverter;
import io.oasp.gastronomy.restaurant.offermanagement.batch.impl.offerimport.writer.OfferWriter;
import io.oasp.gastronomy.restaurant.offermanagement.logic.api.to.OfferEto;

/**
 *
 * This class defines batch configuration for {@link OfferImportJobTest} in an annotations based way for jobs and steps.
 * Bean step 1 contains a skipListener to log skipped items while reading. It could be used instead of XML-config in
 * beans-offerimport.xml
 *
 * @author sroeger
 */

public class OfferImportConfig {

  @Bean
  public JobBuilderFactory jobBuilderFactory(JobRepository jobRepository) {

    return new JobBuilderFactory(jobRepository);
  }

  @Bean
  public StepBuilderFactory stepBuilderFactory(JobRepository jobRepository,
      PlatformTransactionManager transactionManager) {

    return new StepBuilderFactory(jobRepository, transactionManager);
  }

  @Bean
  public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader reader, ItemProcessor processor,
      ItemWriter writer, SkipListener skipListener) {

    /* it handles bunches of 2 units and skips 1 error of types given below */
    return stepBuilderFactory.get("step1").chunk(2).reader(reader).processor(processor).writer(writer).faultTolerant()
        .skipLimit(1).skip(IncorrectTokenCountException.class).skip(FlatFileParseException.class).listener(skipListener)
        .build();

  }

  @Bean
  public Job job1(JobBuilderFactory jobs, Step step1) {

    return jobs.get("job1").incrementer(new RunIdIncrementer()).flow(step1).end().build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<OfferCsv> reader(@Value("#{jobParameters[pathToFile]}") String pathToFile)
      throws MalformedURLException {

    FlatFileItemReader<OfferCsv> reader = new FlatFileItemReader<OfferCsv>();
    reader.setResource(new ClassPathResource(pathToFile));

    reader.setLineMapper(new DefaultLineMapper<OfferCsv>() {
      {
        setLineTokenizer(new DelimitedLineTokenizer() {
          {
            setNames(new String[] { "name", "description", "state", "meal_id", "sidedish_id", "drink_id", "price" });
          }
        });
        setFieldSetMapper(new BeanWrapperFieldSetMapper<OfferCsv>() {
          {
            setTargetType(OfferCsv.class);

          }
        });
      }
    });
    return reader;
  }

  @Bean
  public ItemProcessor<OfferCsv, OfferEto> processor() {

    return new OfferItemConverter();
  }

  @Bean
  public ItemWriter<OfferEto> writer() {

    return new OfferWriter();
  }

  @Bean
  public SkipListener skipListener() {

    return new CustomSkipListener();

  }

}