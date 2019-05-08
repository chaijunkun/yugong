package com.taobao.yugong.extractor.oracle;

import com.taobao.yugong.common.db.meta.TableMetaGenerator;
import com.taobao.yugong.common.model.ExtractStatus;
import com.taobao.yugong.common.model.YuGongContext;
import com.taobao.yugong.common.model.position.Position;
import com.taobao.yugong.common.model.record.Record;
import com.taobao.yugong.exception.YuGongException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 支持增量数据的记录
 * @author agapple 2013-11-21 上午10:26:33
 */
@Slf4j
public class OracleRecRecordExtractor extends AbstractOracleRecordExtractor {

    private YuGongContext context;

    public OracleRecRecordExtractor(YuGongContext context) {
        this.context = context;
    }

    @Override
    public void start() {
        super.start();

        if (context.getRunMode().isClear()) {
            clearIncPosition();
        } else {
            markIncPosition();
        }
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public List<Record> extract() throws YuGongException {
        return new ArrayList<Record>();
    }

    @Override
    public Position ack(List<Record> records) throws YuGongException {
        return null;
    }

    @Override
    public ExtractStatus status() {
        return ExtractStatus.TABLE_END;// 直接返回退出
    }

    /**
     * 做一下inc的增量标记
     */
    protected Position markIncPosition() {
        // change by文疏
        String CREATE_MLOG_FORMAT = "CREATE MATERIALIZED VIEW LOG ON {0}.{1} with {2}";

        String schemaName = context.getTableMeta().getSchema();
        String tableName = context.getTableMeta().getName();
        String extKey = context.getTableMeta().getExtKey();
        String createMlogSql = null;
        // 支持不分表
        if (StringUtils.isBlank(extKey)) {
            // 只处理主键时,可以不带上including new values,可以减少非主键变更时的一条mlog记录
            createMlogSql = MessageFormat.format(CREATE_MLOG_FORMAT, new Object[]{schemaName, tableName,
                    " primary key, sequence"});
        } else {
            createMlogSql = MessageFormat.format(CREATE_MLOG_FORMAT, new Object[]{schemaName, tableName,
                    "sequence (" + extKey + ") including new values"});
        }

        String mlogName = TableMetaGenerator.getMLogTableName(context.getSourceDs(), schemaName, tableName);
        if (mlogName == null) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(context.getSourceDs());
            jdbcTemplate.execute(createMlogSql);
            // 基于MLOG不需要返回position
            log.info("create mlog successed. sql : {}", createMlogSql);
        } else {
            log.warn("mlog[{}] is exist, just have fun. ", mlogName);
        }
        return null;
    }

    protected void clearIncPosition() {
        String schemaName = context.getTableMeta().getSchema();
        String tableName = context.getTableMeta().getName();
        String mlogName = TableMetaGenerator.getMLogTableName(context.getSourceDs(), schemaName, tableName);
        if (mlogName != null) {
            String DROP_MLOG_FORMAT = "DROP MATERIALIZED VIEW LOG ON {0}.{1}";
            String dropMlogSql = MessageFormat.format(DROP_MLOG_FORMAT, new Object[]{
                    context.getTableMeta().getSchema(), context.getTableMeta().getName()});

            JdbcTemplate jdbcTemplate = new JdbcTemplate(context.getSourceDs());
            jdbcTemplate.execute(dropMlogSql);
            log.info("drop mlog successed. sql : {}", dropMlogSql);
        } else {
            log.warn("table[{}.{}] mlog is not exist, just have fun. ", schemaName, tableName);
        }
    }
}
