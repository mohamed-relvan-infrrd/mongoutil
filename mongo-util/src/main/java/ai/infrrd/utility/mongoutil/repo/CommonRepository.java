package ai.infrrd.utility.mongoutil.repo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CommonRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public <T> Optional<T> findById(String id, Class<T> tClass) {
        return Optional.ofNullable(mongoTemplate.findById(id, tClass));
    }

    public <T> Optional<T> findOne(Query query, Class<T> tClass) {
        return Optional.ofNullable(mongoTemplate.findOne(query, tClass));
    }

    public <T> T insert(T objectToSave){
        return mongoTemplate.insert(objectToSave);
    }

    public <T> T insert(T objectToSave, String collectionName){
        return mongoTemplate.insert(objectToSave, collectionName);
    }

    public <T> List<T> findAll(Query query, Class<T> tClass) {
        return mongoTemplate.find(query, tClass);
    }

    public <T,R> List<R> findAllByAggregate(Aggregation aggregation, Class<T> inputClass, Class<R> outputClass){
        AggregationResults<R> result = mongoTemplate.aggregate(aggregation, inputClass, outputClass);
        return result.getMappedResults();
    }

    public void bulkExecute(BulkOperations bulkOperations) {
        bulkOperations.execute();
    }

    public <T> BulkOperations getBulkOperations(BulkOperations.BulkMode bulkMode, Class<T> tClass) {
        return mongoTemplate.bulkOps(bulkMode, tClass);
    }

    public <T> UpdateResult update(Query query, Update update, Class<T> tClass) {
        return mongoTemplate.updateFirst(query, update, tClass);
    }

    public <T> MongoCollection<Document> getCollection(Class<T> tClass) {
        return mongoTemplate.getCollection(tClass.getAnnotation(org.springframework.data.mongodb.core.mapping.Document.class).collection());
    }

    public <T> long getDocumentCount(Query query, Class<T> tClass) {
        return mongoTemplate.count(query, tClass);
    }
}
