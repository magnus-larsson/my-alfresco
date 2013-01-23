package se.vgregion.alfresco.migration;

import java.util.List;

import org.springframework.stereotype.Service;

@Service("migratorService")
public class MigratorServiceImpl implements MigratorService {

  private List<MigratorTask> _migratorTasks;

  public void setMigratorTasks(final List<MigratorTask> migratorTasks) {
    _migratorTasks = migratorTasks;
  }

  @Override
  public void migrate() {
    for (final MigratorTask migratorTask : _migratorTasks) {
      migratorTask.executeMigration();
    }
  }

}
