package dev.educery.storage;

import java.util.*;
import org.springframework.data.repository.CrudRepository;

/**
 * Associates a storage mechanism with its stored model type.
 * @param <StorageType> a kind of storage CrudRepository
 * @param <ModelType> a kind of Model
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class StorageMechanism<ModelType, StorageType extends CrudRepository<ModelType, Long>> {

    public StorageMechanism(StorageType store, Class<StorageType> type, Class<?> modelType) {
        this.store = store;
        this.type = type;
        this.modelType = modelType;
    }

    private final StorageType store;
    public StorageType getStore() { return this.store; }

    private final Class<StorageType> type;
    public Class<StorageType> getStorageType() { return this.type; }

    private final Class<?> modelType;
    public Class<?> getModelType() { return this.modelType; }

    /**
     * @param <StorageType> a storage type
     * @param modelType a model type
     * @return a storage mechanism for a given model type
     */
    public static <StorageType extends CrudRepository> StorageType get(Class<?> modelType) {
        return (Registry.Instance == null) ? null : (StorageType) Registry.Instance.getModelStorage(modelType);
    }

    /**
     * @param <StorageType> a storage type
     * @param storeType a storage type
     * @return a storage mechanism for a given storage type
     */
    public static <StorageType extends CrudRepository> StorageType getStorage(Class<StorageType> storeType) {
        return (Registry.Instance == null) ? null : (StorageType) Registry.Instance.getStorage(storeType);
    }

    /**
     * A storage mechanism registry. Provides access to the configured storage mechanisms.
     */
    public static class Registry {

        private final HashMap<String, StorageMechanism> map = new HashMap();
        private final HashMap<String, StorageMechanism> modelMap = new HashMap();

        /**
         * @param <StorageType> a kind of Repository
         * @param storeType a storage type
         * @return a registered JPA Repository for a given storage type
         */
        public <StorageType extends CrudRepository> StorageType getStorage(Class<StorageType> storeType) {
            return (StorageType) map.get(storeType.getName()).getStore();
        }

        /**
         * @param modelType a model type
         * @return a registered JPA Repository for a given model type
         */
        public CrudRepository getModelStorage(Class<?> modelType) {
            return (CrudRepository) modelMap.get(modelType.getName()).getStore();
        }

        /**
         * @param modelType a model type
         * @return a registered storage type for a given model type
         */
        public Class<?> getStorageType(Class<?> modelType) {
            return modelMap.get(modelType.getName()).getStorageType();
        }

        /**
         * The singular registry.
         */
        static Registry Instance = null;

        /**
         * Registers storage beans.
         * @param beans the storage beans
         * @return a new Registry
         */
        public static Registry with(StorageMechanism... beans) {
            Registry result = new Registry();
            for (StorageMechanism bean : beans) {
                result.register(bean);
            }
            Instance = result;
            return result;
        }

        public void register(StorageMechanism bean) {
            map.put(bean.getStorageType().getName(), bean);
            modelMap.put(bean.getModelType().getName(), bean);
        }

        /**
         * @return a count of the registered stores
         */
        public int size() { return this.map.size(); }

    } // Registry

} // StorageMechanism
