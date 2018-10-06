package com.github.monosoul.gitskipworktree;

import static java.util.stream.Stream.generate;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.actions.VcsContext;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

class AbstractWorkTreeAction_UpdateTest {

    private static final int LIMIT = 10;

    @Mock
    private VcsContext vcsContext;
    @Mock
    private Presentation presentation;
    @Mock
    private Project project;
    @Mock
    private ProjectLevelVcsManager vcsManager;

    @BeforeEach
    void setUp() {
        initMocks(this);

        doReturn(project).when(vcsContext).getProject();
        doReturn(vcsManager).when(project).getComponent(ProjectLevelVcsManager.class);
        doReturn(true).when(vcsManager).hasActiveVcss();
    }

    @ParameterizedTest
    @MethodSource("skipWorkTreeCommandStream")
    void shouldMakeDisabledAndInvisibleWhenProjectIsNull(final SkipWorkTreeCommand command) {
        when(vcsContext.getProject()).thenReturn(null);

        abstractWorkTreeAction(command).update(vcsContext, presentation);

        verify(presentation).setEnabledAndVisible(false);
        verifyNoMoreInteractions(presentation);
    }

    @ParameterizedTest
    @MethodSource("skipWorkTreeCommandStream")
    void shouldMakeDisabledAndInvisibleWhenDoesNotHaveActiveVcss(final SkipWorkTreeCommand command) {
        when(vcsManager.hasActiveVcss()).thenReturn(false);

        abstractWorkTreeAction(command).update(vcsContext, presentation);

        verify(presentation).setEnabledAndVisible(false);
        verifyNoMoreInteractions(presentation);
    }

    @ParameterizedTest
    @MethodSource("skipWorkTreeCommandStream")
    void shouldMakeDisabledAndVisibleWhenBackgroundVcsOperationIsRunning(final SkipWorkTreeCommand command) {
        when(vcsManager.isBackgroundVcsOperationRunning()).thenReturn(true);

        abstractWorkTreeAction(command).update(vcsContext, presentation);

        verify(presentation).setEnabled(false);
        verify(presentation).setVisible(true);
        verifyNoMoreInteractions(presentation);
    }

    @ParameterizedTest
    @MethodSource("skipWorkTreeCommandStream")
    void shouldMakeEnabledAndVisible(final SkipWorkTreeCommand command) {
        when(vcsManager.isBackgroundVcsOperationRunning()).thenReturn(false);

        abstractWorkTreeAction(command).update(vcsContext, presentation);

        verify(presentation).setEnabled(true);
        verify(presentation).setVisible(true);
        verifyNoMoreInteractions(presentation);
    }

    private AbstractWorkTreeAction abstractWorkTreeAction(final SkipWorkTreeCommand command) {
        return new TestAbstractWorkTreeActionImpl(command);
    }

    private static Stream<SkipWorkTreeCommand> skipWorkTreeCommandStream() {
        return generate(Util::getRandomSkipWorkTreeCommand).limit(LIMIT);
    }
}