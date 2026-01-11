package com.nttd.banking.customer.infrastructure.config;

import com.nttd.banking.customer.infrastructure.adapter.out.persistence.entity.BusinessCustomerEntity;
import com.nttd.banking.customer.infrastructure.adapter.out.persistence.entity.PersonalCustomerEntity;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.data.util.TypeInformation;

/**
 * MongoDB configuration for polymorphic customer entity handling.
 * Configures type mapping based on customerType field for existing documents.
 *
 * @author NTT Data
 * @version 1.0
 */
@Configuration
@EnableReactiveMongoRepositories(basePackages =
    "com.nttd.banking.customer.infrastructure.adapter.out.persistence.repository")
public class MongoConfig {

  /**
   * Creates a custom ReactiveMongoTemplate with proper type mapping.
   * Uses the auto-configured ReactiveMongoDatabaseFactory from Spring Boot
   * which reads the database name from the MongoDB URI in application properties.
   *
   * @param databaseFactory the auto-configured database factory (from URI)
   * @param context the MongoDB mapping context
   * @param conversions custom conversions
   * @return configured ReactiveMongoTemplate
   */
  @Bean
  public ReactiveMongoTemplate reactiveMongoTemplate(
      ReactiveMongoDatabaseFactory databaseFactory,
      MongoMappingContext context,
      MongoCustomConversions conversions) {

    MappingMongoConverter converter =
        new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context);
    converter.setCustomConversions(conversions);
    converter.setTypeMapper(new CustomerTypeMapper());
    converter.afterPropertiesSet();

    return new ReactiveMongoTemplate(databaseFactory, converter);
  }

  /**
   * Custom type mapper that uses customerType field to determine concrete class.
   * Falls back to _class field if customerType is not present.
   */
  private static class CustomerTypeMapper extends DefaultMongoTypeMapper {

    public CustomerTypeMapper() {
      super(DEFAULT_TYPE_KEY);
    }

    @Override
    public <T> TypeInformation<? extends T> readType(org.bson.conversions.Bson source,
                                                      TypeInformation<T> basicType) {
      if (source instanceof Document document) {
        // First try to get type from customerType field
        String customerType = document.getString("customerType");
        if (customerType != null) {
          Class<?> targetClass = switch (customerType) {
            case "PERSONAL" -> PersonalCustomerEntity.class;
            case "BUSINESS" -> BusinessCustomerEntity.class;
            default -> null;
          };
          if (targetClass != null) {
            @SuppressWarnings("unchecked")
            TypeInformation<? extends T> result =
                (TypeInformation<? extends T>) TypeInformation.of(targetClass);
            return result;
          }
        }
      }
      // Fall back to default behavior (using _class field)
      return super.readType(source, basicType);
    }
  }
}
