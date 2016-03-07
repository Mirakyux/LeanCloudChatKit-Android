package cn.leanclud.imkit.viewholder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMSingleMessageQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.leanclud.imkit.R;
import cn.leanclud.imkit.cache.UnreadCountCache;
import cn.leanclud.imkit.event.LCIMConversationItemClickEvent;
import cn.leanclud.imkit.utils.LCIMConversationUtils;
import cn.leanclud.imkit.utils.LCIMEmotionHelper;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/10/8.
 */
public class LCIMConversationItemHolder extends LCIMCommonViewHolder {

  ImageView avatarView;
  TextView unreadView;
  TextView messageView;
  TextView timeView;
  TextView nameView;
  RelativeLayout avatarLayout;
  LinearLayout contentLayout;

  public LCIMConversationItemHolder(ViewGroup root) {
    super(root.getContext(), root, R.layout.lcim_conversation_item);
    initView();
  }

  public void initView() {
    avatarView = (ImageView) itemView.findViewById(R.id.conversation_item_iv_avatar);
    nameView = (TextView) itemView.findViewById(R.id.conversation_item_tv_name);
    timeView = (TextView) itemView.findViewById(R.id.conversation_item_tv_time);
    unreadView = (TextView) itemView.findViewById(R.id.conversation_item_tv_unread);
    messageView = (TextView) itemView.findViewById(R.id.conversation_item_tv_message);
    avatarLayout = (RelativeLayout) itemView.findViewById(R.id.conversation_item_layout_avatar);
    contentLayout = (LinearLayout) itemView.findViewById(R.id.conversation_item_layout_content);
  }

  @Override
  public void bindData(Object o) {
    final AVIMConversation conversation = (AVIMConversation) o;
    if (null != conversation) {

      LCIMConversationUtils.getConversationName(conversation, new AVCallback<String>() {
        @Override
        protected void internalDone0(String s, AVException e) {
          nameView.setText(s);
        }
      });

      LCIMConversationUtils.getConversationIcon(conversation, new AVCallback<String>() {
        @Override
        protected void internalDone0(String s, AVException e) {
          if (!TextUtils.isEmpty(s)) {
            Picasso.with(getContext()).load(s).into(avatarView);
          }
        }
      });

      int num = UnreadCountCache.getInstance().getUnreadCount(conversation.getConversationId());
      unreadView.setText(num + "");
      unreadView.setVisibility(num > 0 ? View.VISIBLE : View.GONE);

      conversation.getLastMessage(new AVIMSingleMessageQueryCallback() {
        @Override
        public void done(AVIMMessage avimMessage, AVIMException e) {
          if (null != avimMessage) {
            Date date = new Date(avimMessage.getTimestamp());
            SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
            timeView.setText(format.format(date));
            messageView.setText(getMessageeShorthand(getContext(), avimMessage));
          } else {
            timeView.setText("");
            messageView.setText("");
          }
        }
      });
      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          EventBus.getDefault().post(new LCIMConversationItemClickEvent(conversation.getConversationId()));
        }
      });
    }
  }

  public static ViewHolderCreator HOLDER_CREATOR = new ViewHolderCreator<LCIMConversationItemHolder>() {
    @Override
    public LCIMConversationItemHolder createByViewGroupAndType(ViewGroup parent, int viewType) {
      return new LCIMConversationItemHolder(parent);
    }
  };

  private static CharSequence getMessageeShorthand(Context context, AVIMMessage message) {
    if (message instanceof AVIMTypedMessage) {
      AVIMReservedMessageType type = AVIMReservedMessageType.getAVIMReservedMessageType(
        ((AVIMTypedMessage) message).getMessageType());
      switch (type) {
        case TextMessageType:
          return LCIMEmotionHelper.replace(context, ((AVIMTextMessage) message).getText());
        case ImageMessageType:
          return "[图片]";
        case LocationMessageType:
          return "[位置]";
        case AudioMessageType:
          return "[语音]";
        default:
          return "[未知]";
      }
    } else {
      return message.getContent();
    }
  }
}
