package com.hyphenate.easeui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;
import com.hyphenate.easeui.widget.EaseChatExtendMenu.EaseChatExtendMenuItemClickListener;
import com.hyphenate.easeui.widget.EaseChatPrimaryMenuBase.EaseChatPrimaryMenuListener;


import com.hyphenate.easeui.widget.bqmmgif.BQMMGifManager;
import com.melink.bqmmsdk.sdk.BQMM;
import com.melink.bqmmsdk.ui.keyboard.BQMMKeyboard;
import com.melink.bqmmsdk.ui.keyboard.IGifButtonClickListener;

import java.util.List;
import java.util.Timer;

/**
 * input menu
 * 
 * including below component:
 *    EaseChatPrimaryMenu: main menu bar, text input, send button
 *    EaseChatExtendMenu: grid menu with image, file, location, etc
 *    EaseEmojiconMenu: emoji icons
 */
public class EaseChatInputMenu extends LinearLayout {
    public boolean isEmojiClick;
    FrameLayout primaryMenuContainer, emojiconMenuContainer;
    protected EaseChatPrimaryMenuBase chatPrimaryMenu;
    /**
     * BQMM集成
     * 将原有的表情键盘改成BQMMKeyboard
     */
    public BQMMKeyboard emojiconMenu;
    protected EaseChatExtendMenu chatExtendMenu;
    public FrameLayout chatExtendMenuContainer;
    protected LayoutInflater layoutInflater;

    private Handler handler = new Handler();
    private ChatInputMenuListener listener;
    private Context context;
    private boolean inited;

    public EaseChatInputMenu(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public EaseChatInputMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EaseChatInputMenu(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        layoutInflater.inflate(R.layout.ease_widget_chat_input_menu, this);
        primaryMenuContainer = (FrameLayout) findViewById(R.id.primary_menu_container);
        emojiconMenuContainer = (FrameLayout) findViewById(R.id.emojicon_menu_container);
        chatExtendMenuContainer = (FrameLayout) findViewById(R.id.extend_menu_container);

         // extend menu
         chatExtendMenu = (EaseChatExtendMenu) findViewById(R.id.extend_menu);
        

    }

    /**c
     * init view 
     * 
     * This method should be called after registerExtendMenuItem(), setCustomEmojiconMenu() and setCustomPrimaryMenu().
     * @param emojiconGroupList --will use default if null
     */
    @SuppressLint("InflateParams")
    public void init(List<EaseEmojiconGroupEntity> emojiconGroupList) {
        if(inited){
            return;
        }
        // primary menu, use default if no customized one
        if(chatPrimaryMenu == null){
            chatPrimaryMenu = (EaseChatPrimaryMenu) layoutInflater.inflate(R.layout.ease_layout_chat_primary_menu, null);
        }
        primaryMenuContainer.addView(chatPrimaryMenu);

        // emojicon menu, use default if no customized one
        /**
         * BQMM集成
         * 将表情栏改为BQMM的键盘，并传递给BQMM
         */
        emojiconMenu = (BQMMKeyboard) layoutInflater.inflate(R.layout.ease_layout_emojicon_menu, null);
        BQMM.getInstance().setKeyboard(emojiconMenu, new IGifButtonClickListener() {
            @Override
            public void didClickGifTab() {
                hideExtendMenuContainer();
                BQMM.getInstance().getEditView().requestFocus();
                showKeyBoard(getContext());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BQMMGifManager.getInstance(getContext()).showTrending();
                    }
                },300);
            }
        });
        emojiconMenuContainer.addView(emojiconMenu);
        processChatMenu();
        chatExtendMenu.init();
        
        inited = true;
    }
    public void showKeyBoard(Context context){
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0,InputMethodManager.SHOW_FORCED);
    }
    public void init(){
        init(null);
    }

    /**
     * set custom emojicon menu
     * @param customEmojiconMenu
     * BQMM集成
     * 这里移除对表情栏的自定义功能
     */

    /**
     * set custom primary menu
     * @param customPrimaryMenu
     */
    public void setCustomPrimaryMenu(EaseChatPrimaryMenuBase customPrimaryMenu){
        this.chatPrimaryMenu = customPrimaryMenu;
    }
    
    public EaseChatPrimaryMenuBase getPrimaryMenu(){
        return chatPrimaryMenu;
    }
    
    public EaseChatExtendMenu getExtendMenu(){
        return chatExtendMenu;
    }


    /**
     * BQMM集成
     * 将返回类型修改为BQMMKeyboard
     */
    public BQMMKeyboard getEmojiconMenu(){
        return emojiconMenu;
    }
    

    /**
     * register menu item
     * 
     * @param name
     *            item name
     * @param drawableRes
     *            background of item
     * @param itemId
     *             id
     * @param listener
     *            on click event of item
     */
    public void registerExtendMenuItem(String name, int drawableRes, int itemId,
            EaseChatExtendMenuItemClickListener listener) {
        chatExtendMenu.registerMenuItem(name, drawableRes, itemId, listener);
    }

    /**
     * register menu item
     * 
     * @param nameRes
     *            resource id of item name
     * @param drawableRes
     *            background of item
     * @param itemId
     *             id
     * @param listener
     *            on click event of item
     */
    public void registerExtendMenuItem(int nameRes, int drawableRes, int itemId,
            EaseChatExtendMenuItemClickListener listener) {
        chatExtendMenu.registerMenuItem(nameRes, drawableRes, itemId, listener);
    }


    protected void processChatMenu() {
        // send message button
        chatPrimaryMenu.setChatPrimaryMenuListener(new EaseChatPrimaryMenuListener() {

            @Override
            public void onSendBtnClicked(String content) {
                if (listener != null)
                    listener.onSendMessage(content);
            }

            @Override
            public void onToggleVoiceBtnClicked() {
                hideExtendMenuContainer();
            }

            @Override
            public void onToggleExtendClicked() {
                toggleMore();
            }

            @Override
            public void onToggleEmojiconClicked() {
                toggleEmojicon();
            }

            @Override
            public void onEditTextClicked() {
                hideExtendMenuContainer();
            }


            @Override
            public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
                if(listener != null){
                    return listener.onPressToSpeakBtnTouch(v, event);
                }
                return false;
            }
        });

        /**
         * BQMM集成
         * 这里移除对表情栏监听器的设置
         */


    }
    
   
    /**
     * insert text
     * @param text
     */
    public void insertText(String text){
        getPrimaryMenu().onTextInsert(text);
    }

    /**
     * show or hide extend menu
     * 
     */
    protected void toggleMore() {
        if (chatExtendMenuContainer.getVisibility() == View.GONE) {
            hideKeyboard();
            handler.postDelayed(new Runnable() {
                public void run() {
                    chatExtendMenuContainer.setVisibility(View.VISIBLE);
                    chatExtendMenu.setVisibility(View.VISIBLE);
                    emojiconMenu.setVisibility(View.GONE);
                }
            }, 50);
        } else {
            if (emojiconMenu.getVisibility() == View.VISIBLE) {
                emojiconMenu.setVisibility(View.GONE);
                chatExtendMenu.setVisibility(View.VISIBLE);
            } else {
                chatExtendMenuContainer.setVisibility(View.GONE);
            }
        }
    }

    /**
     * show or hide emojicon
     */
    protected void toggleEmojicon() {
        if (chatExtendMenuContainer.getVisibility() == View.GONE) {
            isEmojiClick = true;
            hideKeyboard();
            chatExtendMenu.setVisibility(View.GONE);
        } else {
            if (emojiconMenu.getVisibility() == View.VISIBLE) {
                chatExtendMenuContainer.setVisibility(View.GONE);
                emojiconMenu.setVisibility(View.GONE);
            } else {
                chatExtendMenu.setVisibility(View.GONE);
                emojiconMenu.setVisibility(View.VISIBLE);
            }

        }
    }

    /**
     * hide keyboard
     */
    private void hideKeyboard() {
        chatPrimaryMenu.hideKeyboard();
    }

    /**
     * hide extend menu
     */
    public void hideExtendMenuContainer() {
        chatExtendMenu.setVisibility(View.GONE);
        emojiconMenu.setVisibility(View.GONE);
        chatExtendMenuContainer.setVisibility(View.GONE);
        chatPrimaryMenu.onExtendMenuContainerHide();
    }

    /**
     * when back key pressed
     * 
     * @return false--extend menu is on, will hide it first
     *         true --extend menu is off 
     */
    public boolean onBackPressed() {
        BQMMGifManager.getInstance(getContext()).updateSearchModeAndSearchUIWithStatus(BQMMGifManager.BQMM_SEARCH_MODE_STATUS_KEYBOARD_HIDE);
        if (chatExtendMenuContainer.getVisibility() == View.VISIBLE) {
            hideExtendMenuContainer();
            return false;
        } else {
            return true;
        }

    }
    

    public void setChatInputMenuListener(ChatInputMenuListener listener) {
        this.listener = listener;
    }

    public interface ChatInputMenuListener {
        /**
         * when send message button pressed
         * 
         * @param content
         *            message content
         */
        void onSendMessage(String content);
        
        /**
         * when big icon pressed
         * @param emojicon
         */
        void onBigExpressionClicked(EaseEmojicon emojicon);

        /**
         * when speak button is touched
         * @param v
         * @param event
         * @return
         */
        boolean onPressToSpeakBtnTouch(View v, MotionEvent event);
    }
    
}
