package pl.kempa.saska.service.util;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceIdGeneratorUnitTest {

  @InjectMocks
  private ResourceIdGenerator generator;

  @Test
  void generateId_generate10000IdsForTheSameFileName_noCollisions() {
    // given
    String fileName = "test_song.mp3";

    // when
    List<Integer> resourceIds =
        IntStream.range(0, 999)
            .mapToObj(i -> generator.generateId(fileName))
            .toList();

    // then
    assertThat(resourceIds).doesNotHaveDuplicates();
  }
}