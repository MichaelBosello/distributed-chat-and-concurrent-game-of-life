package actorgameoflife.actors;

import actorgameoflife.gameoflifegui.mainpanel.GameOfLifeGUI;
import actorgameoflife.gameoflifegui.mainpanel.MainPanel;
import actorgameoflife.gameoflifegui.mainpanel.MainPanelObserver;
import actorgameoflife.gameoflifegui.matrixtoimage.ConvertToImage;
import actorgameoflife.messages.BoardMessage;
import actorgameoflife.messages.gui.BoardUpdatedMessage;
import actorgameoflife.messages.gui.ScrollMessage;
import actorgameoflife.messages.gui.StartMessage;
import actorgameoflife.messages.gui.StopMessage;
import akka.actor.AbstractActor;
import akka.actor.Props;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class GUIActor extends AbstractActor {

    GameOfLifeGUI gui;

    public GUIActor(int boardRow, int boardColumn, int visualizedRow, int visualizedColumn) {
        try {
            SwingUtilities.invokeAndWait( () -> {
                gui = new MainPanel(boardRow, boardColumn, visualizedRow, visualizedColumn);
                gui.addObserver(new MainPanelObserver() {
                    @Override
                    public void startEvent() {
                        getContext().parent().tell(new StartMessage(), getSelf());
                    }

                    @Override
                    public void stopEvent() {
                        getContext().parent().tell(new StopMessage(), getSelf());
                    }

                    @Override
                    public void boardUpdated() {
                        //getContext().parent().tell(new BoardUpdatedMessage(), getSelf());
                    }

                    @Override
                    public void scrollEvent(int x, int y) {
                        getContext().parent().tell(new ScrollMessage(x, y), getSelf());
                    }
                });
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    static Props props(int boardRow, int boardColumn, int visualizedRow, int visualizedColumn) {
        return Props.create(GUIActor.class, () -> new GUIActor(boardRow, boardColumn, visualizedRow, visualizedColumn));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(BoardMessage.class, msg -> {
            SwingUtilities.invokeLater( () -> {
                gui.updateBoard(ConvertToImage.boardToImage(msg.getBoard()));
                gui.updateLivingCellLabel(msg.getLivingCell());
            });
        }).build();
    }
}
