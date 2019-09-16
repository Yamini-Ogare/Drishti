package ognora.drishti;

public enum  SpeechDataHolder {
    INSTANCE;

    private String data;

    public static String getData() {
        final  String currentData = INSTANCE.data;
        INSTANCE.data = null;
        return currentData;
    }

    public static void setData(final String data) {

        INSTANCE.data = data;
    }

    public static boolean hasData(){
        return (INSTANCE.data !=null) ;
    }
}
