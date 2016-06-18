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
         * contentView的OnClickListener会让聊天气泡的长按事件失效，所以要在这里设置一个OnLongClickListener，让它调用bubbleLayout的长按事件
         */
        contentView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                bubbleLayout.performLongClick();
                return true;
            }
        });
    }

    @Override
    public void onSetUpView() {
        /**
         * BQMM集成
         * 加载消息的逻辑较为复杂，我们把它写在另一个函数中
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
        contentView.showMessage(message.getMsgId(),((TextMessageBody)message.getBody()).getMessage(),msgType,msgData);
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
