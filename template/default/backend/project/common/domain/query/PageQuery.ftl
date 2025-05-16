package ${projectPackage}.${projectNameDot}.common.domain.query;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页查询参数基类，分页查询入参和返回基类
 *
 * @param <T> 必须是Entity或者是VO
 */
@Data
@Schema(description = "分页查询参数基类")
public class PageQuery<T> implements IPage<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 2101820826443930585L;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", defaultValue = "1")
    private Long pageNum = 1L;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", defaultValue = "10")
    private Long pageSize = 10L;

    /**
     * 排序字段列表
     */
    @Schema(description = "排序字段列表")
    private List<OrderItem> orders;

    /**
     * 分页数据
     */
    @Schema(description = "分页数据", hidden = true)
    private List<T> records;

    /**
     * 记录总数
     */
    @Schema(description = "记录总数", hidden = true)
    private long total;

    @Schema(hidden = true)
    private Long size;

    @Schema(hidden = true)
    private Long current;

    @Schema(hidden = true)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long pages;

    @Override
    public long getSize() {
        return this.pageSize;
    }

    @Override
    public IPage<T> setSize(long size) {
        this.pageSize = size;
        return this;
    }

    @Override
    public long getCurrent() {
        return pageNum;
    }

    @Override
    public IPage<T> setCurrent(long current) {
        this.pageSize = current;
        return this;
    }

    @Override
    public List<T> getRecords() {
        return this.records;
    }

    @Override
    public PageQuery<T> setRecords(List<T> records) {
        this.records = records;
        return this;
    }

    @Override
    public long getTotal() {
        return this.total;
    }

    @Override
    public PageQuery<T> setTotal(long total) {
        this.total = total;
        return this;
    }

    @Override
    public List<OrderItem> orders() {
        return this.orders;
    }

}
