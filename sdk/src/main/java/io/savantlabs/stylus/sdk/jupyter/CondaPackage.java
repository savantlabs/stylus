package io.savantlabs.stylus.sdk.jupyter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Data;

@Data
public class CondaPackage {
  //List<String> properties; //list has 3 el -> name, version, channel
  private String name, version, channel;
  public boolean hasPackage(String name) throws InterruptedException {
    AtomicBoolean foundFlag = new AtomicBoolean(false);  //found flag for whether package found or not
    AtomicBoolean checkFlag = new AtomicBoolean(false);  //check flag for checking the lines
    Process process =
        ShellRunner.runScript(
            (p) ->
                (line) -> {
                  if(line.startsWith("# Name                    Version                   Build  Channel")){
                    checkFlag.set(true);
                  }else if(checkFlag.get() && line.split(" ")[0].equals(name)){
                    foundFlag.set(true);
                  }
                },
            (p) ->
                (line) -> {
                },
            "list_pkg.sh",name);
    process.waitFor();
    return foundFlag.get();
  }


}
