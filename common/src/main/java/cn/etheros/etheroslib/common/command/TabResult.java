package cn.etheros.etheroslib.common.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 补全结果合并
 *
 * <p>去重、前缀过滤、按字典序排序；前缀匹配忽略大小写，输出保留原始值
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class TabResult {

    private TabResult() {
    }

    public static List<String> merge(Collection<String> candidates, String prefix) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        String normalized = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<String>(candidates.size());
        Set<String> seen = new HashSet<String>();
        for (String candidate : candidates) {
            if (candidate == null || candidate.isEmpty()) {
                continue;
            }
            String key = candidate.toLowerCase(Locale.ROOT);
            if (!key.startsWith(normalized)) {
                continue;
            }
            if (!seen.add(key)) {
                continue;
            }
            result.add(candidate);
        }
        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result;
    }
}
