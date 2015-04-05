package com.letsdeveloper.games;

import static com.letsdeveloper.games.GameLoop.FRAME_DURATION;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GameLoopTest {

	@Mock
	private Game<TestInput> testGame;

	@Mock
	private InputHandler<TestInput> testInputHandler;

	@Mock
	private Timer testTimer;
	private int time;

	@InjectMocks
	private GameLoop<TestInput> uut;

	@Before
	public void setUp() {
		time = 0;
		when(testTimer.getCurrentTime()).then(i -> {
			int oldTime = time;
			time += FRAME_DURATION;
			return oldTime;
		});
	}

	@Test
	public void doesNothingIfGameIsNotRunning() {
		when(testGame.isRunning()).thenReturn(false);

		uut.run();

		verify(testGame, never()).update(any());
	}

	@Test
	public void invokesOneUpdateIfGameIsRunning() {
		when(testGame.isRunning()).thenReturn(true, false);

		uut.run();

		verify(testGame).update(any());
	}

	@Test
	public void invokesUpdateAsLongAsGameIsRunning() {
		when(testGame.isRunning()).thenReturn(true, true, true, false);

		uut.run();

		verify(testGame, times(3)).update(any());
	}

	@Test
	public void invokesRenderAfterUpdate() {
		when(testGame.isRunning()).thenReturn(true, false);

		uut.run();

		InOrder inOrder = inOrder(testGame);
		inOrder.verify(testGame).update(any());
		inOrder.verify(testGame).render();
	}

	@Test
	public void passesInputToUpdate() {
		TestInput testInput = new TestInput();
		when(testInputHandler.getCurrentInput()).thenReturn(testInput);
		when(testGame.isRunning()).thenReturn(true, false);

		uut.run();

		verify(testGame).update(testInput);
	}

	@Test
	public void skipUpdateIfLoopIsTooFast() {
		when(testGame.isRunning()).thenReturn(true, false);
		when(testTimer.getCurrentTime()).thenReturn(0, 1);

		uut.run();

		verify(testGame, never()).update(any());
		verify(testGame, times(1)).render();
	}

	@Test
	public void doesAdditionalUpdateIfLoopIsTooSlow() {
		when(testGame.isRunning()).thenReturn(true, false);
		when(testTimer.getCurrentTime()).thenReturn(0, 2 * FRAME_DURATION);

		uut.run();

		verify(testGame, times(2)).update(any());
		verify(testGame, times(1)).render();
	}

	@Test
	public void doesExecuteUpdateEventually() {
		when(testGame.isRunning()).thenReturn(true, true, true, false);
		int halfFrameDuration = (int) (FRAME_DURATION / 2);
		when(testTimer.getCurrentTime()).thenReturn(0, halfFrameDuration, 2 * halfFrameDuration, 3 * halfFrameDuration);

		uut.run();

		verify(testGame, times(1)).update(any());
	}

	public static class TestInput {

	}
}
