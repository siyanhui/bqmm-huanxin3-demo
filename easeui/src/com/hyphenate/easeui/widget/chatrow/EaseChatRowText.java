package com.hyphenate.easeui.widget.chatrow;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.R;
import com.hyphenate.exceptions.HyphenateException;
import com.melink.baseframe.utils.DensityUtils;
import com.melink.bqmmsdk.widget.BQMMMessageText;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.melink.bqmmsdk.widget.BQMMMessageText.FACETYPE;
import static com.melink.bqmmsdk.widget.BQMMMessageText.WEBTYPE;

public class EaseChatRowText extends EaseChatRow{

    /**
     * BQMM集成
     * 将该属性的类型更改为BQMMMessageText
     */
    private BQMMMessageText contentView;
    private RelativeLayout bubble;
    public EaseChatRowText(Context context, EMMessage message, int position, BaseAdapter adapter) {
		super(context, message, position, adapter);
	}

	@Override
	protected void onInflateView() {
		inflater.inflate(message.direct() == EMMessage.Direct.RECEIVE ?
				R.layout.ease_row_received_message : R.layout.ease_row_sent_message, this);
	}

	@Override
	protected void onFindViewById() {
        /**
         * BQMM集成
         * 转换类型
         */
        contentView = (BQMMMessageText) findViewById(R.id.tv_chatcontent);
        bubble = (RelativeLayout) findViewById(R.id.bubble);
        /**
         * BQMM集成
         * emojiView的OnClickListener会让聊天气泡的长按事件失效，所以要在这里设置一个OnLongClickListener，让它调用bubbleLayout的长按事件
         */
        contentView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                bubbleLayout.performLongClick();
                return true;
            }
        });
        /**
         * BQMM集成
         * 设置大表情的显示大小
         */
        contentView.setStickerSize(DensityUtils.dip2px(100));
    }


    @Override
    public void onSetUpView() {
        /**
         * BQMM集成
         * 加载消息时的步骤如下：
         * 从消息中解析出Message Type和Message Data，
         * 前者可以是“facetype”或者“emojitype”，分别代表单个大表情和含有表情的文字消息
         * 后者是由BQMMMessageHelper.getMixedMessageData()函数生成的
         * 然后调用BQMMMessageText.showMessage()函数，该函数共四个参数，该消息的一个唯一ID、纯文本消息（说明见下）、Message Type、Message Data
         * 对于由BQMM生成的消息，Message Data即含有该消息的全部内容。但出于兼容性考虑，可能有一些纯文本的历史消息需要显示
         * 这个时候，只需要将纯文本消息作为第二个参数传入，并将Message Type传为空字符串，Message Data传为null，
         * BQMMMessageText会作为一个普通TextView将消息直接展示出来
         */
        String msgType;
        JSONArray msgData = null;
        JSONObject webMsgData = null;
        try {
            msgType = message.getStringAttribute(EaseConstant.BQMM_MESSAGE_KEY_TYPE);
            if (msgType.equals(EaseConstant.BQMM_MESSAGE_TYPE_WEB_STICKER)){
                webMsgData = message.getJSONObjectAttribute(EaseConstant.BQMM_MESSAGE_KEY_CONTENT);
            }else {
                msgData = message.getJSONArrayAttribute(EaseConstant.BQMM_MESSAGE_KEY_CONTENT);
            }
        } catch (HyphenateException e) {
            msgType = "";
            msgData = null;
            webMsgData = null;
        }
        if (message.direct() == EMMessage.Direct.RECEIVE)
            bubble.setBackgroundResource(R.drawable.ease_chatfrom_bg);
        else
            bubble.setBackgroundResource(R.drawable.ease_chatto_bg);
        /**
         * 为大表情时去掉背景框
         */
        if (TextUtils.equals(msgType, FACETYPE) || TextUtils.equals(msgType, WEBTYPE)) {
            bubble.setBackgroundResource(0);
        }
        // 设置内容
        if (TextUtils.equals(msgType, WEBTYPE)){
            contentView.showBQMMGif(webMsgData.optString("data_id"),webMsgData.optString("sticker_url"),webMsgData.optInt("w"),webMsgData.optInt("h"),webMsgData.optInt("is_gif"));
        }else {
            contentView.showMessage(((EMTextMessageBody) message.getBody()).getMessage(), msgType, msgData);
        }


        handleTextMessage();
    }

    protected void handleTextMessage() {
        if (message.direct() == EMMessage.Direct.SEND) {
            setMessageSendCallback();
            switch (message.status()) {
            case CREATE: 
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.GONE);
                break;
            case FAIL:
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                progressBar.setVisibility(View.VISIBLE);
                statusView.setVisibility(View.GONE);
                break;
            default:
               break;
            }
        }else{
            if(!message.isAcked() && message.getChatType() == ChatType.Chat){
                try {
                    EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onUpdateView() {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onBubbleClick() {
        // TODO Auto-generated method stub
        
    }



}
