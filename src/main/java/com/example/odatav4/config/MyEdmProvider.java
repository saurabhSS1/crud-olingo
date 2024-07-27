package com.example.odatav4.config;

import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.edm.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MyEdmProvider extends CsdlAbstractEdmProvider {

    private static final String NAMESPACE = "com.example.odatav4";
    private static final String CONTAINER_NAME = "Container";
    private static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    private static final FullQualifiedName ET_ACCOUNT = new FullQualifiedName(NAMESPACE, "Account");
    private static final FullQualifiedName ET_CONTACT = new FullQualifiedName(NAMESPACE, "Contact");

    private static final String ES_ACCOUNTS = "acqAccounts";
    private static final String ES_CONTACTS = "acqContacts";

    @Override
    public List<CsdlSchema> getSchemas() {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        List<CsdlEntityType> entityTypes = new ArrayList<>();
        entityTypes.add(getEntityType(ET_ACCOUNT));
        entityTypes.add(getEntityType(ET_CONTACT));
        schema.setEntityTypes(entityTypes);

        schema.setEntityContainer(getEntityContainer());

        List<CsdlSchema> schemas = new ArrayList<>();
        schemas.add(schema);

        return schemas;
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        if (entityTypeName.equals(ET_ACCOUNT)) {
            return new CsdlEntityType()
                    .setName(ET_ACCOUNT.getName())
                    .setKey(Collections.singletonList(new CsdlPropertyRef().setName("id")))
                    .setProperties(Arrays.asList(
                            new CsdlProperty().setName("id").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                            new CsdlProperty().setName("accountName").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                            new CsdlProperty().setName("accountType").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                            new CsdlProperty().setName("recordLastModifiedDate").setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName())
                    ));
        } else if (entityTypeName.equals(ET_CONTACT)) {
            return new CsdlEntityType()
                    .setName(ET_CONTACT.getName())
                    .setKey(Collections.singletonList(new CsdlPropertyRef().setName("id")))
                    .setProperties(Arrays.asList(
                            new CsdlProperty().setName("id").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                            new CsdlProperty().setName("contactName").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                            new CsdlProperty().setName("acqAccountId").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                            new CsdlProperty().setName("contactType").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
                            new CsdlProperty().setName("recordLastModifiedDate").setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName())
                    ));
        }
        return null;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
        if (entityContainer.equals(CONTAINER)) {
            if (entitySetName.equals(ES_ACCOUNTS)) {
                return new CsdlEntitySet().setName(ES_ACCOUNTS).setType(ET_ACCOUNT);
            } else if (entitySetName.equals(ES_CONTACTS)) {
                return new CsdlEntitySet().setName(ES_CONTACTS).setType(ET_CONTACT);
            }
        }
        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);

        List<CsdlEntitySet> entitySets = new ArrayList<>();
        entitySets.add(getEntitySet(CONTAINER, ES_ACCOUNTS));
        entitySets.add(getEntitySet(CONTAINER, ES_CONTACTS));

        entityContainer.setEntitySets(entitySets);

        return entityContainer;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {
        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER);
            return entityContainerInfo;
        }
        return null;
    }
}
