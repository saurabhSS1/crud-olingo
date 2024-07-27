package com.example.odatav4.processor;

import com.example.odatav4.entity.AcqContact;
import com.example.odatav4.service.AcqContactService;
import org.apache.olingo.commons.api.data.*;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class AcqContactProcessor implements EntityCollectionProcessor, EntityProcessor {

    @Autowired
    private AcqContactService service;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        System.out.println("Entering readEntityCollection method");
        FilterOption filterOption = uriInfo.getFilterOption();
        OrderByOption orderByOption = uriInfo.getOrderByOption();
        SkipOption skipOption = uriInfo.getSkipOption();
        TopOption topOption = uriInfo.getTopOption();

        String filter = filterOption != null ? filterOption.getText() : null;
        String orderBy = orderByOption != null ? orderByOption.getText() : null;
        Integer skip = skipOption != null ? skipOption.getValue() : null;
        Integer top = topOption != null ? topOption.getValue() : null;

        List<AcqContact> contacts = service.getAllContacts(filter, orderBy, top, skip);

        EntityCollection entityCollection = new EntityCollection();
        for (AcqContact contact : contacts) {
            Entity entity = new Entity()
                    .addProperty(new Property(null, "id", ValueType.PRIMITIVE, contact.getId()))
                    .addProperty(new Property(null, "contactName", ValueType.PRIMITIVE, contact.getContactName()))
                    .addProperty(new Property(null, "acqAccountId", ValueType.PRIMITIVE, contact.getAcqAccountId()))
                    .addProperty(new Property(null, "contactType", ValueType.PRIMITIVE, contact.getContactType()))
                    .addProperty(new Property(null, "recordLastModifiedDate", ValueType.PRIMITIVE, contact.getRecordLastModifiedDate()));
            entityCollection.getEntities().add(entity);
        }

        EdmEntitySet edmEntitySet = ((UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)).getEntitySet();
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        SerializerResult serializedResponse = serializer.entityCollection(serviceMetadata, edmEntitySet.getEntityType(), entityCollection, EntityCollectionSerializerOptions.with().contextURL(ContextURL.with().entitySet(edmEntitySet).build()).build());

        response.setContent(serializedResponse.getContent());
        response.setStatusCode(200);
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        System.out.println("Exiting readEntityCollection method");
    }

    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        System.out.println("Entering readEntity method");
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
        String id = uriResourceEntitySet.getKeyPredicates().get(0).getText();

        AcqContact contact = service.getContactById(id).orElseThrow(() -> new ODataApplicationException("Contact not found", 404, Locale.ENGLISH));

        Entity entity = new Entity()
                .addProperty(new Property(null, "id", ValueType.PRIMITIVE, contact.getId()))
                .addProperty(new Property(null, "contactName", ValueType.PRIMITIVE, contact.getContactName()))
                .addProperty(new Property(null, "acqAccountId", ValueType.PRIMITIVE, contact.getAcqAccountId()))
                .addProperty(new Property(null, "contactType", ValueType.PRIMITIVE, contact.getContactType()))
                .addProperty(new Property(null, "recordLastModifiedDate", ValueType.PRIMITIVE, contact.getRecordLastModifiedDate()));

        ODataSerializer serializer = odata.createSerializer(responseFormat);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        SerializerResult serializedContent = serializer.entity(serviceMetadata, edmEntitySet.getEntityType(), entity, EntitySerializerOptions.with().contextURL(ContextURL.with().serviceRoot(URI.create(request.getRawBaseUri() + "/odata")).keyPath(id).build()).build());

        response.setContent(serializedContent.getContent());
        response.setStatusCode(200);
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        System.out.println("Exiting readEntity method");
    }

    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        System.out.println("Entering createEntity method");

        EdmEntitySet edmEntitySet = ((UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)).getEntitySet();
        System.out.println("EntitySet: " + edmEntitySet.getName());

        // Deserialize the request payload into an Olingo entity
        ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
        System.out.println("Deserializer created");

        DeserializerResult result = deserializer.entity(request.getBody(), edmEntitySet.getEntityType());
        System.out.println("Request payload deserialized");

        Entity requestEntity = result.getEntity();
        System.out.println("Request entity obtained: " + requestEntity);

        // Logging the requestEntity properties
        System.out.println("Request Entity Properties: ");
        for (Property property : requestEntity.getProperties()) {
            System.out.println("Property Name: " + property.getName() + ", Property Value: " + property.getValue());
        }

        // Convert Olingo entity to JPA AcqContact entity
        Property idProperty = requestEntity.getProperty("id");
        Property contactNameProperty = requestEntity.getProperty("contactName");
        Property acqAccountIdProperty = requestEntity.getProperty("acqAccountId");
        Property contactTypeProperty = requestEntity.getProperty("contactType");
        Property recordLastModifiedDateProperty = requestEntity.getProperty("recordLastModifiedDate");

        // Log each property and check for null values
        if (idProperty == null) {
            System.out.println("id property is null");
        } else {
            System.out.println("id property value: " + idProperty.getValue());
        }
        if (contactNameProperty == null) {
            System.out.println("contactName property is null");
        } else {
            System.out.println("contactName property value: " + contactNameProperty.getValue());
        }
        if (acqAccountIdProperty == null) {
            System.out.println("acqAccountId property is null");
        } else {
            System.out.println("acqAccountId property value: " + acqAccountIdProperty.getValue());
        }
        if (contactTypeProperty == null) {
            System.out.println("contactType property is null");
        } else {
            System.out.println("contactType property value: " + contactTypeProperty.getValue());
        }
        if (recordLastModifiedDateProperty == null) {
            System.out.println("recordLastModifiedDate property is null");
        } else {
            System.out.println("recordLastModifiedDate property value: " + recordLastModifiedDateProperty.getValue());
        }

        if (idProperty == null || contactNameProperty == null || acqAccountIdProperty == null || contactTypeProperty == null || recordLastModifiedDateProperty == null) {
            throw new ODataApplicationException("Missing required properties", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
        }

        AcqContact newContact = new AcqContact();
        newContact.setId((String) idProperty.getValue());
        newContact.setContactName((String) contactNameProperty.getValue());
        newContact.setAcqAccountId((String) acqAccountIdProperty.getValue());
        newContact.setContactType((String) contactTypeProperty.getValue());

        // Parse the date with the correct format
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        OffsetDateTime dateTime = OffsetDateTime.parse((String) recordLastModifiedDateProperty.getValue(), formatter);
        newContact.setRecordLastModifiedDate(dateTime);

        // Save the entity
        System.out.println("Saving new contact");
        AcqContact createdContact = service.createContact(newContact);

        // Convert JPA entity back to Olingo entity
        Entity responseEntity = new Entity()
                .addProperty(new Property(null, "id", ValueType.PRIMITIVE, createdContact.getId()))
                .addProperty(new Property(null, "contactName", ValueType.PRIMITIVE, createdContact.getContactName()))
                .addProperty(new Property(null, "acqAccountId", ValueType.PRIMITIVE, createdContact.getAcqAccountId()))
                .addProperty(new Property(null, "contactType", ValueType.PRIMITIVE, createdContact.getContactType()))
                .addProperty(new Property(null, "recordLastModifiedDate", ValueType.PRIMITIVE, createdContact.getRecordLastModifiedDate().toString()));

        // Serialize the response
        System.out.println("Serializing response");
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntitySet.getEntityType(), responseEntity, EntitySerializerOptions.with().contextURL(ContextURL.with().serviceRoot(URI.create(request.getRawBaseUri() + "/odata")).keyPath(createdContact.getId()).build()).build());

        response.setContent(serializedResponse.getContent());
        response.setStatusCode(201);
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        System.out.println("Exiting createEntity method");
    }

    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        System.out.println("Entering updateEntity method");
        EdmEntitySet edmEntitySet = ((UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)).getEntitySet();
        String id = ((UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)).getKeyPredicates().get(0).getText();

        // Deserialize the request payload into an Olingo entity
        ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
        DeserializerResult result = deserializer.entity(request.getBody(), edmEntitySet.getEntityType());
        Entity requestEntity = result.getEntity();

        // Convert Olingo entity to JPA AcqContact entity
        AcqContact updatedContact = new AcqContact();
        updatedContact.setId(id);
        updatedContact.setContactName((String) requestEntity.getProperty("contactName").getValue());
        updatedContact.setAcqAccountId((String) requestEntity.getProperty("acqAccountId").getValue());
        updatedContact.setContactType((String) requestEntity.getProperty("contactType").getValue());

        // Parse the date with the correct format
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        OffsetDateTime dateTime = OffsetDateTime.parse((String) requestEntity.getProperty("recordLastModifiedDate").getValue(), formatter);
        updatedContact.setRecordLastModifiedDate(dateTime);

        // Update the entity
        AcqContact updated = service.updateContact(id, updatedContact);

        // Convert JPA entity back to Olingo entity
        Entity responseEntity = new Entity()
                .addProperty(new Property(null, "id", ValueType.PRIMITIVE, updated.getId()))
                .addProperty(new Property(null, "contactName", ValueType.PRIMITIVE, updated.getContactName()))
                .addProperty(new Property(null, "acqAccountId", ValueType.PRIMITIVE, updated.getAcqAccountId()))
                .addProperty(new Property(null, "contactType", ValueType.PRIMITIVE, updated.getContactType()))
                .addProperty(new Property(null, "recordLastModifiedDate", ValueType.PRIMITIVE, updated.getRecordLastModifiedDate().toString()));

        // Serialize the response
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntitySet.getEntityType(), responseEntity, EntitySerializerOptions.with().contextURL(ContextURL.with().serviceRoot(URI.create(request.getRawBaseUri() + "/odata")).keyPath(id).build()).build());

        response.setContent(serializedResponse.getContent());
        response.setStatusCode(200);
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        System.out.println("Exiting updateEntity method");
    }

    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
        System.out.println("Entering deleteEntity method");
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
        String id = uriResourceEntitySet.getKeyPredicates().get(0).getText();

        // Delete the entity
        service.deleteContact(id);

        response.setStatusCode(204);
        System.out.println("Exiting deleteEntity method");
    }
}
