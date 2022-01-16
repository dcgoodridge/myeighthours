package myeighthours;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MehOptions {

    private static final Logger LOG = LoggerFactory.getLogger(MehOptions.class);

    public enum WEBDRIVER { PHANTOMJS, HTMLUNIT }

    private final String username;

    private final String password;

    private final WEBDRIVER webdriver;

    private MehOptions(Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.webdriver = builder.webdriver;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public WEBDRIVER getWebdriver() {
        return webdriver;
    }

    public static class Builder {

        private String username = "";
        private String password = "";
        private WEBDRIVER webdriver = WEBDRIVER.PHANTOMJS;

        public Builder username(String username){
            this.username = username;
            return this;
        }

        public Builder password(String password){
            this.password = password;
            return this;
        }

        public Builder webdriver(WEBDRIVER webdriver){
            this.webdriver = webdriver;
            return this;
        }

        public MehOptions build(){
            return new MehOptions(this);
        }

    }


}
