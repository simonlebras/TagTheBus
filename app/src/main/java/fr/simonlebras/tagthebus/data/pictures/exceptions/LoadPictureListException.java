package fr.simonlebras.tagthebus.data.pictures.exceptions;

public class LoadPictureListException extends Exception {
    public LoadPictureListException() {
        super("Fail to load pictures from Content Provider");
    }
}
