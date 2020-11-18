package ma.markware.charybdis.demo.spring.repository;

import java.util.UUID;
import ma.markware.charybdis.CqlTemplate;
import ma.markware.charybdis.batch.Batch;
import ma.markware.charybdis.demo.spring.domain.AbstractMessage;
import ma.markware.charybdis.demo.spring.domain.Message;
import ma.markware.charybdis.demo.spring.domain.MessageByChatRoom;
import ma.markware.charybdis.demo.spring.domain.MessageByChatRoom_Table;
import ma.markware.charybdis.demo.spring.domain.Message_Table;
import ma.markware.charybdis.query.PageRequest;
import ma.markware.charybdis.query.PageResult;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepository {

  private final CqlTemplate cqlTemplate;

  public MessageRepository(final CqlTemplate cqlTemplate) {
    this.cqlTemplate = cqlTemplate;
  }

  public void create(AbstractMessage message) {
    Batch batch = cqlTemplate.batch().logged();
    cqlTemplate.crud(batch).create(Message_Table.message, new Message(message));
    cqlTemplate.crud(batch).create(MessageByChatRoom_Table.message_by_chatroom, new MessageByChatRoom(message));
    batch.execute();
  }

  public PageResult<MessageByChatRoom> getMessagesByChatRoomPaged(UUID chatRoomId, PageRequest pageRequest) {
    return cqlTemplate.crud()
                      .find(MessageByChatRoom_Table.message_by_chatroom, MessageByChatRoom_Table.chatRoomId.eq(chatRoomId), pageRequest);
  }
}
