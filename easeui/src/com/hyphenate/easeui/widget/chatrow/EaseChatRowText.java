package com.hyphenate.easeui.widget.chatrow;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.exceptions.HyphenateException;
import com.melink.baseframe.utils.DensityUtils;
import com.melink.bqmmsdk.widget.BQMMMessageText;

import android.content.Context;
import android.text.Spannable;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import org.json.JSONArray;

public class EaseChatRowText extends EaseChatRow{

    /**
     * BQMM集成
     * 将该属性的类型更改为BQMMMessageText
     */
	private BQMMMessageText contentView;

    public EaseChatRowText(Context context, EMMessage message, int position, BaseAdapter adapter) {
		super(context, message, position, adapter);
	}

	@Override
	protected void onInflatView() {
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
        JSONArray msgData;
        try {
            msgType = message.getStringAttribute(EaseConstant.BQMM_MESSAGE_KEY_TYPE);
            msgData = message.getJSONArrayAttribute(EaseConstant.BQMM_MESSAGE_KEY_CONTENT);
        } catch (HyphenateException e) {
            msgType = "";
            msgData = null;
        }
        // 设置内容
        contentView.showMessage(((EMTextMessageBody) message.getBody()).getMessage(), msgType, msgData);

        handleTextMessage();
    }

    protected void handleTextMessage() {
        if (message.direct() == EMMessage.Direct.SEND) {
            setMessageSendCallback();
            switch (message.status()) {
            case CREATE: 
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                // 发送消息
//                sendMsgInBackground(message);
                break;
            case SUCCESS: // 发送成功
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.GONE);
                break;
            case FAIL: // 发送失败
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS: // 发送中
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
