package com.easemob.easeui.widget.chatrow;

import android.content.Context;
import android.view.View;
import android.widget.BaseAdapter;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.TextMessageBody;
import com.easemob.easeui.R;
import com.easemob.exceptions.EaseMobException;
import com.melink.bqmmsdk.widget.BQMMMessageText;

import org.json.JSONArray;
import org.json.JSONException;

public class EaseChatRowText extends EaseChatRow{

    private BQMMMessageText contentView;

    public EaseChatRowText(Context context, EMMessage message, int position, BaseAdapter adapter) {
		super(context, message, position, adapter);
	}

	@Override
	protected void onInflatView() {
		inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ?
				R.layout.ease_row_received_message : R.layout.ease_row_sent_message, this);
	}

	@Override
	protected void onFindViewById() {
        contentView = (BQMMMessageText) findViewById(R.id.tv_chatcontent);
        contentView.setBigEmojiShowSize(200);
        /**
         * BQMM集成
         * 我们用这个View代替了原先的bubbleLayout，所以要在这里设置一个OnLongClickListener，让它调用和bubbleLayout相同的逻辑
         */
        contentView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onBubbleLongClick(message);
                }
                return true;
            }
        });
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
            msgType = message.getStringAttribute("txt_msgType");
            msgData = message.getJSONArrayAttribute("msg_data");
        } catch (EaseMobException e1) {
            msgType = "";
            msgData = null;
        }
        contentView.showMessage(message.getMsgId(), ((TextMessageBody) message.getBody()).getMessage(), msgType, msgData);
        handleTextMessage();
    }

    protected void handleTextMessage() {
        if (message.direct == EMMessage.Direct.SEND) {
            setMessageSendCallback();
            switch (message.status) {
            case CREATE:
                progressBar.setVisibility(View.VISIBLE);
                statusView.setVisibility(View.GONE);
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
                    EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                    message.isAcked = true;
                } catch (EaseMobException e) {
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

    /**
     * BQMM集成
     * 对存储在Json中的消息进行解析
     *
     * @param msg_Data
     * @return 消息的文本表达
     */
    public static String parseMsgData(JSONArray msg_Data) {
        StringBuilder sendMsg = new StringBuilder();
        try {
            for (int i = 0; i < msg_Data.length(); i++) {
                JSONArray childArray = msg_Data.getJSONArray(i);
                if (childArray.get(1).equals("1")) {
                    sendMsg.append("[").append(childArray.get(0)).append("]");
                } else {
                    sendMsg.append(childArray.get(0));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sendMsg.toString();
    }
}
