package cn.etheros.etheroslib.common.command.provider;

import cn.etheros.etheroslib.common.command.TabContext;
import cn.etheros.etheroslib.common.command.TabProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 固定候选
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class StaticTabProvider implements TabProvider {

    private final List<String> candidates;

    public StaticTabProvider(Collection<String> candidates) {
        this.candidates = Collections.unmodifiableList(new ArrayList<String>(candidates));
    }

    public StaticTabProvider(String... candidates) {
        this(Arrays.asList(candidates));
    }

    @Override
    public Collection<String> suggest(TabContext context) {
        return candidates;
    }
}
