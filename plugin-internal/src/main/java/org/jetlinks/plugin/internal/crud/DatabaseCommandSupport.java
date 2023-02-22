package org.jetlinks.plugin.internal.crud;

import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.jetlinks.core.event.EventBus;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

public class DatabaseCommandSupport {

    private RDBTableMetadata table;
    private DatabaseOperator database;

    private EventBus eventBus;


    public Mono<Integer> delete(Collection<String> id) {
        return database
                .dml()
                .delete(table)
                .where(cdt -> cdt.in("id", id))
                .execute()
                .reactive();
    }

    public Mono<Integer> update(String id, Map<String, Object> data) {
        return database
                .dml()
                .update(table)
                .where(ctd -> ctd.is("id", id))
                .set(data)
                .execute()
                .reactive();
    }

    public Mono<Integer> insert(Collection<Map<String, Object>> data) {
        return database
                .dml()
                .insert(table)
                .values(data)
                .execute()
                .reactive();
    }

    public Mono<Integer> save(Collection<Map<String, Object>> data) {
        return database
                .dml()
                .upsert(table)
                .values(data)
                .execute()
                .reactive()
                .map(SaveResult::getTotal);
    }


}
