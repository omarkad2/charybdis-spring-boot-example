package ma.markware.charybdis.demo.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import ma.markware.charybdis.demo.domain.ChatRoom;
import ma.markware.charybdis.demo.domain.ChatRoomByCode;
import ma.markware.charybdis.demo.domain.MessageByChatRoom;
import ma.markware.charybdis.demo.dto.Page;
import ma.markware.charybdis.demo.mapper.MessageMapper;
import ma.markware.charybdis.demo.repository.ChatRoomRepository;
import ma.markware.charybdis.demo.repository.MessageRepository;
import ma.markware.charybdis.demo.websocket.ChatMessagePayload;
import ma.markware.charybdis.query.PageRequest;
import ma.markware.charybdis.query.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ChatRoomService {

  private final ChatRoomRepository chatRoomRepository;
  private final MessageRepository messageRepository;
  private final MessageMapper messageMapper;

  public ChatRoomService(final ChatRoomRepository chatRoomRepository, final MessageRepository messageRepository, final MessageMapper messageMapper) {
    this.chatRoomRepository = chatRoomRepository;
    this.messageRepository = messageRepository;
    this.messageMapper = messageMapper;
  }

  public Page<ChatMessagePayload> getMessagesInChatRoom(final UUID chatRoomId, final Integer limit, final String pagingState) {
    Page<ChatMessagePayload> result = new Page<>();
    PageRequest pageRequest = null;
    if (limit != null && StringUtils.isNotBlank(pagingState)) {
      pageRequest = PageRequest.of(ByteBuffer.wrap(pagingState.getBytes(StandardCharsets.UTF_8)), limit);
    }
    PageResult<MessageByChatRoom> pageResult = messageRepository.getMessagesByChatRoomPaged(chatRoomId, pageRequest);
    result.setPagingState(pageResult.getPagingState());
    result.setResults(pageResult.getResults()
                                .stream()
                                .map(messageMapper::map)
                                .collect(Collectors.toList()));
    return result;
  }

  public UUID getChatRoomId(UUID senderId, UUID receiverId) {
    int chatRoomCode = computeChatRoomCode(senderId, receiverId);
    Optional<ChatRoomByCode> chatRoom = chatRoomRepository.findChatRoomByCode(chatRoomCode);
    if(chatRoom.isPresent()) {
      return chatRoom.get().getChatRoomId();
    }
    ChatRoom newChatRoom = new ChatRoom(chatRoomCode, senderId, Set.of(senderId, receiverId), true);
    chatRoomRepository.create(newChatRoom);
    return newChatRoom.getChatRoomId();
  }

  private int computeChatRoomCode(final UUID senderId, final UUID receiverId) {
    int chatRoomCode;
    if (senderId.compareTo(receiverId) > 0) {
      chatRoomCode = Objects.hash(senderId, receiverId);
    } else {
      chatRoomCode = Objects.hash(receiverId, senderId);
    }
    return chatRoomCode;
  }
}
