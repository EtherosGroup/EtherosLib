package cn.etheros.etheroslib.common.command.provider;

import cn.etheros.etheroslib.common.command.TabContext;
import cn.etheros.etheroslib.common.command.TabProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 整数区间候选，闭区间
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class RangeTabProvider implements TabProvider {

    private final List<String> candidates;

    public RangeTabProvider(int from, int to) {
        if (from > to) {
            this.candidates = Collections.emptyList();
            return;
        }
        List<String> values = new ArrayList<String>(to - from + 1);
        for (int value = from; value <= to; value++) {
            values.add(Integer.toString(value));
        }
        this.candidates = Collections.unmodifiableList(values);
    }

    @Override
    public Collection<String> suggest(TabContext context) {
        return candidates;
    }
}
