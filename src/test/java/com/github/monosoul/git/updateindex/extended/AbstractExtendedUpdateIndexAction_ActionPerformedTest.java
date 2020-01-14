package com.github.monosoul.git.updateindex.extended;

import static com.github.monosoul.git.updateindex.extended.Util.getRandomSkipWorkTreeCommand;
import static com.intellij.openapi.application.ApplicationManager.setApplication;
import static com.intellij.openapi.util.Disposer.dispose;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.log4j.Level.DEBUG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.intellij.mock.MockApplication;
import com.intellij.mock.MockProject;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.actions.VcsContext;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.Git;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.val;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;

class AbstractExtendedUpdateIndexAction_ActionPerformedTest {

    private static final int LIMIT = 10;

    private TestDisposable parent;
    private MockApplication application;
    private MockProject project;

    @Mock
    private Git git;
    @Mock
    private VcsContext vcsContext;
    @Mock
    private ProjectLevelVcsManager vcsManager;
    @Mock
    private VcsDirtyScopeManager dirtyScopeManager;
    @Mock
    private Function<Entry<VirtualFile, List<VirtualFile>>, GitLineHandler> gitLineHandlerCreator;
    @Mock
    private GitLineHandler gitLineHandler;
    @Mock
    private GitCommandResult gitCommandResult;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        initMocks(this);
        parent = new TestDisposable();

        application = new MockApplication(parent);
        setApplication(application, parent);
        application.registerService(Git.class, git, parent);

        project = new MockProject(null, parent);

        project.registerService(ProjectLevelVcsManager.class, vcsManager);
        project.registerService(VcsDirtyScopeManager.class, dirtyScopeManager);
        doReturn(project).when(vcsContext).getProject();

        doReturn(gitLineHandler).when(gitLineHandlerCreator).apply(any(Entry.class));
        doReturn(gitCommandResult).when(git).runCommand(gitLineHandler);
    }

    @AfterEach
    void tearDown() {
        dispose(parent);
    }

    @ParameterizedTest
    @MethodSource("skipWorkTreeCommandStream")
    void doNotPerformActionIfProjectIsNull(final ExtendedUpdateIndexCommand command) {
        when(vcsContext.getProject()).thenReturn(null);

        abstractWorkTreeAction(command).actionPerformed(vcsContext);

        verify(vcsContext).getProject();
        verifyNoMoreInteractions(vcsContext);
        verifyZeroInteractions(git);
    }

    @ParameterizedTest
    @MethodSource("skipWorkTreeCommandStream")
    void doNothingIfNoRootForFile(final ExtendedUpdateIndexCommand command) {
        val file = mock(VirtualFile.class);

        when(vcsContext.getSelectedFilesStream()).then((Answer<Stream<VirtualFile>>) i -> Stream.of(file));
        when(vcsManager.getVcsRootFor(file)).thenReturn(null);

        abstractWorkTreeAction(command).actionPerformed(vcsContext);

        verify(vcsContext).getProject();
        verify(vcsContext, times(2)).getSelectedFilesStream();
        verify(vcsManager).getVcsRootFor(file);
        verify(dirtyScopeManager).fileDirty(file);
        verifyNoMoreInteractions(vcsContext, vcsManager, dirtyScopeManager);
        verifyZeroInteractions(git);
    }

    @ParameterizedTest
    @MethodSource("virtualFileListAndCommandStream")
    void executeGitCommandAndLogNothingIfSuccessful(final List<VirtualFile> files, final ExtendedUpdateIndexCommand command) {
        val root = mock(VirtualFile.class);

        when(vcsContext.getSelectedFilesStream()).then((Answer<Stream<VirtualFile>>) i -> files.stream());
        when(vcsManager.getVcsRootFor(any(VirtualFile.class))).thenReturn(root);
        when(gitCommandResult.success()).thenReturn(true);

        abstractWorkTreeAction(command).actionPerformed(vcsContext);

        verify(vcsContext).getProject();
        verify(vcsContext, times(2)).getSelectedFilesStream();
        verify(vcsManager, times(files.size())).getVcsRootFor(any(VirtualFile.class));
        verify(git).runCommand(gitLineHandler);
        verify(gitCommandResult).success();
        verify(dirtyScopeManager, times(files.size())).fileDirty(any(VirtualFile.class));
        verifyNoMoreInteractions(vcsContext, vcsManager, git, gitCommandResult, dirtyScopeManager);
    }

    @ParameterizedTest
    @MethodSource("virtualFileListAndCommandStream")
    void executeGitCommandAndLogErrorIfNotSuccessful(final List<VirtualFile> files, final ExtendedUpdateIndexCommand command) {
        val root = mock(VirtualFile.class);
        val captor = ArgumentCaptor.forClass(LoggingEvent.class);
        val appender = configureAppender();
        val errorMessage = randomAlphabetic(LIMIT);

        when(vcsContext.getSelectedFilesStream()).then((Answer<Stream<VirtualFile>>) i -> files.stream());
        when(vcsManager.getVcsRootFor(any(VirtualFile.class))).thenReturn(root);
        when(gitCommandResult.success()).thenReturn(false);
        when(gitCommandResult.getErrorOutput()).thenReturn(singletonList(errorMessage));

        abstractWorkTreeAction(command).actionPerformed(vcsContext);

        verify(vcsContext).getProject();
        verify(vcsContext, times(2)).getSelectedFilesStream();
        verify(vcsManager, times(files.size())).getVcsRootFor(any(VirtualFile.class));
        verify(git).runCommand(gitLineHandler);
        verify(gitCommandResult).success();
        verify(gitCommandResult).getErrorOutput();
        verify(dirtyScopeManager, times(files.size())).fileDirty(any(VirtualFile.class));
        verify(appender).doAppend(captor.capture());
        verifyNoMoreInteractions(vcsContext, vcsManager, git, gitCommandResult, dirtyScopeManager);

        assertThat(captor.getValue().getMessage()).isEqualTo(errorMessage);
    }

    private Appender configureAppender() {
        val appender = mock(Appender.class);
        val logger = Logger.getLogger(AbstractExtendedUpdateIndexAction.class);
        logger.addAppender(appender);
        logger.setLevel(DEBUG);

        return appender;
    }

    private AbstractExtendedUpdateIndexAction abstractWorkTreeAction(final ExtendedUpdateIndexCommand command) {
        val abstractWorkTreeAction = spy(new TestAbstractExtendedUpdateIndexActionImpl(command));
        doReturn(gitLineHandlerCreator).when(abstractWorkTreeAction).gitLineHandlerCreator(project);

        return abstractWorkTreeAction;
    }

    private static Stream<Arguments> virtualFileListAndCommandStream() {
        return generate(() -> (Arguments) () ->
                new Object[]{
                        virtualFileStream().limit(nextInt(1, LIMIT)).collect(toList()),
                        getRandomSkipWorkTreeCommand()
                }
        ).limit(LIMIT);
    }

    private static Stream<VirtualFile> virtualFileStream() {
        return generate(() -> mock(VirtualFile.class)).limit(LIMIT);
    }

    private static Stream<ExtendedUpdateIndexCommand> skipWorkTreeCommandStream() {
        return generate(Util::getRandomSkipWorkTreeCommand).limit(LIMIT);
    }
}