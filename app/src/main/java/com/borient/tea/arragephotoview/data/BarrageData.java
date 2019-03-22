package com.borient.tea.arragephotoview.data;

import com.orient.tea.barragephoto.model.DataSource;

/**
 * 弹幕数据
 *
 * Created by wangjie on 2019/3/22.
 */

public class BarrageData implements DataSource {

    public static final int BARRAGE_TEXT = 1;
    public static final int BARRAGE_IMAGE_TEXT = 2;

    private String content;
    private int type;

    public BarrageData(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getType() {
        return type;
    }

    // 是不是不需要 考虑删除
    @Override
    public long getShowTime() {
        return 0;
    }
}
