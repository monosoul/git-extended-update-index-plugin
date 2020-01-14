package com.github.monosoul.git.updateindex.extended;

import static com.intellij.openapi.util.Disposer.dispose;
import static java.util.stream.Stream.generate;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.intellij.mock.MockProject;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.actions.VcsContext;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

class AbstractExtendedUpdateIndexAction_UpdateTest {

    private static final int LIMIT = 10;

    private TestDisposable parent;
    private MockProject project;

    @Mock
    private VcsContext vcsContext;
    @Mock
    private Presentation presentation;
    @Mock
    private ProjectLevelVcsManager vcsManager;

    @BeforeEach
    void setUp() {
        initMocks(this);

        parent = new TestDisposable();
        project = new MockProject(null, parent);

        project.registerService(ProjectLevelVcsManager.class, vcsManager);
        doReturn(project).when(vcsContext).getProject();

        doReturn(true).when(vcsManager).hasActiveVcss();
    }

    @AfterEach
    void tearDown() {
        dispose(parent);
    }

    @ParameterizedTest
    @MethodSource("skipWorkTreeCommandStream")
    void should_make_the_menu_item_invisible_and_disabled_when_the_project_is_null(
            final ExtendedUpdateIndexCommand command
    ) {
        when(vcsContext.getProject()).thenReturn(null);

        abstractWorkTreeAction(command).update(vcsContext, presentation);

        verify(presentation).setEnabledAndVisible(false);
        verifyNoMoreInteractions(presentation);
    }

    @ParameterizedTest
    @MethodSource("skipWorkTreeCommandStream")
    void should_make_the_menu_item_invisible_and_disabled_when_the_project_does_not_have_an_active_vcs(
            final ExtendedUpdateIndexCommand command
    ) {
        when(vcsManager.hasActiveVcss()).thenReturn(false);

        abstractWorkTreeAction(command).update(vcsContext, presentation);

        verify(presentation).setEnabledAndVisible(false);
        verifyNoMoreInteractions(presentation);
    }

    @ParameterizedTest
    @MethodSource("skipWorkTreeCommandStream")
    void should_make_the_menu_item_visible_and_disabled_when_a_background_operation_is_running(
            final ExtendedUpdateIndexCommand command
    ) {
        when(vcsManager.isBackgroundVcsOperationRunning()).thenReturn(true);

        abstractWorkTreeAction(command).update(vcsContext, presentation);

        verify(presentation).setEnabled(false);
        verify(presentation).setVisible(true);
        verifyNoMoreInteractions(presentation);
    }

    @ParameterizedTest
    @MethodSource("skipWorkTreeCommandStream")
    void should_make_the_menu_item_visible_and_enabled(final ExtendedUpdateIndexCommand command) {
        when(vcsManager.isBackgroundVcsOperationRunning()).thenReturn(false);

        abstractWorkTreeAction(command).update(vcsContext, presentation);

        verify(presentation).setEnabled(true);
        verify(presentation).setVisible(true);
        verifyNoMoreInteractions(presentation);
    }

    private AbstractExtendedUpdateIndexAction abstractWorkTreeAction(final ExtendedUpdateIndexCommand command) {
        return new TestAbstractExtendedUpdateIndexActionImpl(command);
    }

    private static Stream<ExtendedUpdateIndexCommand> skipWorkTreeCommandStream() {
        return generate(Util::getRandomSkipWorkTreeCommand).limit(LIMIT);
    }
}