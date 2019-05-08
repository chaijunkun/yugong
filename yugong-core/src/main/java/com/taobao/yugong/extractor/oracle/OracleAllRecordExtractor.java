package com.taobao.yugong.extractor.oracle;

import com.taobao.yugong.common.model.ExtractStatus;
import com.taobao.yugong.common.model.ProgressStatus;
import com.taobao.yugong.common.model.YuGongContext;
import com.taobao.yugong.common.model.position.IdPosition;
import com.taobao.yugong.common.model.position.Position;
import com.taobao.yugong.common.model.record.Record;
import com.taobao.yugong.common.utils.YuGongUtils;
import com.taobao.yugong.exception.YuGongException;
import com.taobao.yugong.positioner.RecordPositioner;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 支持ALL模式的oracle数据同步
 *
 * @author agapple 2013-9-26 下午4:20:49
 */
@Slf4j
public class OracleAllRecordExtractor extends AbstractOracleRecordExtractor {

    private IdPosition currentPostion;
    private YuGongContext context;

    /**
     * 增量记录接口
     */
    @Setter
    private AbstractOracleRecordExtractor markExtractor;
    /**
     * 全量抽取接口
     */
    @Setter
    private AbstractOracleRecordExtractor fullExtractor;
    /**
     * 增量抽取接口
     */
    @Setter
    private AbstractOracleRecordExtractor incExtractor;

    @Setter
    private RecordPositioner positioner;

    public OracleAllRecordExtractor(YuGongContext context) {
        this.context = context;

    }

    @Override
    public void start() {
        super.start();
        currentPostion = (IdPosition) context.getLastPosition();
        if (currentPostion == null) {
            currentPostion = new IdPosition();
        }

        boolean hasMark = true;
        if (!currentPostion.isInHistory(ProgressStatus.MARK)) {
            hasMark = false;
            // 进入mark阶段
            currentPostion.setCurrentProgress(ProgressStatus.MARK);
            context.setLastPosition(currentPostion);
            positioner.persist(currentPostion);
            markExtractor.start();
        }

        // 如果是full跑完后，切到ALL时
        if (!currentPostion.isInHistory(ProgressStatus.FULLING) || !hasMark) {
            if (fullExtractor.isStart()) {
                throw new YuGongException("fullExtractor should start after markIncPosition , pls check");
            }
            // 进入fulling阶段
            currentPostion.setCurrentProgress(ProgressStatus.FULLING);
            context.setLastPosition(currentPostion);
            positioner.persist(currentPostion);
            fullExtractor.start();
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (incExtractor.isStart()) {
            incExtractor.stop();
        }

        if (fullExtractor.isStart()) {
            fullExtractor.stop();
        }

        if (markExtractor.isStart()) {
            markExtractor.stop();
        }
    }

    @Override
    public List<Record> extract() throws YuGongException {
        List<Record> result = null;
        if (fullExtractor.isStart()) {
            result = fullExtractor.extract();

            if (YuGongUtils.isEmpty(result) && fullExtractor.status() == ExtractStatus.TABLE_END) {
                log.info("table [{}] full extractor is end , next auto start inc extractor", context.getTableMeta()
                        .getFullName());
                if (incExtractor.isStart()) {
                    throw new YuGongException("incExtractor should start after fullExtractor , pls check");
                }
                // 关闭全量
                fullExtractor.stop();
                // 进入incing阶段
                currentPostion.setCurrentProgress(ProgressStatus.INCING);
                // 启动增量
                context.setLastPosition(currentPostion);
                positioner.persist(currentPostion);
                incExtractor.start();
            } else {
                // 直接返回
                return result;
            }
        } else if (!incExtractor.isStart()) {
            // 进入incing阶段
            currentPostion.setCurrentProgress(ProgressStatus.INCING);
            // 启动增量
            context.setLastPosition(currentPostion);
            positioner.persist(currentPostion);
            incExtractor.start();
        }

        if (incExtractor.isStart()) {
            result = incExtractor.extract();
        }

        return result;
    }

    @Override
    public Position ack(List<Record> records) throws YuGongException {
        if (incExtractor.isStart()) {
            return incExtractor.ack(records);
        } else if (fullExtractor.isStart()) {
            return fullExtractor.ack(records);
        } else {
            throw new YuGongException("extractor is stop");
        }
    }

    @Override
    public ExtractStatus status() {
        if (incExtractor.isStart()) {
            return incExtractor.status();
        } else if (fullExtractor.isStart()) {
            return fullExtractor.status();
        } else {
            throw new YuGongException("extractor is stop");
        }
    }

}
