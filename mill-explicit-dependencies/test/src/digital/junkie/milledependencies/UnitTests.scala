/*
 * Copyright 2026 David Akermann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package digital.junkie.milledependencies

import mill.testkit.UnitTester
import utest.*
import scala.annotation.experimental

@experimental
object UnitTests extends TestSuite {
  def tests: Tests = Tests {

    test("scala-library should not be reported as undeclared") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(UnusedDependencies, resourceFolder / "unit-test-project")
        .scoped { eval =>
          val Right(UnitTester.Result(UndeclaredReport(undeclared, _), _)) =
            eval(
              UnusedDependencies.undeclaredCompileDependenciesAnon
            ).runtimeChecked

          assert(!undeclared.exists(_.presentation.contains("scala-library")))
        }
    }

    test("runtime dependency should not be marked as unused") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(UnusedDependencies, resourceFolder / "unit-test-project")
        .scoped { eval =>
          val Right(UnitTester.Result(UnusedReport(unused, _), _)) =
            eval(
              UnusedDependencies.unusedCompileDependenciesAnon
            ).runtimeChecked
          assert(!unused.exists(_.presentation.contains("slf4j-simple")))
        }
    }

    test("unused dependency is detected") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(UnusedDependencies, resourceFolder / "unit-test-project")
        .scoped { eval =>
          val Right(UnitTester.Result(UnusedReport(unused, _), _)) =
            eval(
              UnusedDependencies.unusedCompileDependenciesAnon
            ).runtimeChecked
          val expected = "co.fs2::fs2-core:3.12.2"
          assert(unused.exists(_.presentation == expected))
        }
    }

    test("used dependency is not marked as unused") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(UnusedDependencies, resourceFolder / "unit-test-project")
        .scoped { eval =>
          val Right(UnitTester.Result(UnusedReport(unused, _), _)) =
            eval(
              UnusedDependencies.unusedCompileDependenciesAnon
            ).runtimeChecked
          assert(!unused.exists(_.fullName.startsWith("cats-effect")))
        }
    }

    test("undeclared dependency is detected") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(UndeclaredDependencies, resourceFolder / "unit-test-project")
        .scoped { eval =>
          val Right(UnitTester.Result(UndeclaredReport(undeclared, _), _)) =
            eval(
              UndeclaredDependencies.undeclaredCompileDependenciesAnon
            ).runtimeChecked
          assert(undeclared.exists(_.fullName.startsWith("cats-effect")))
        }
    }

    test("declared dependency is not marked as undeclared") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(UndeclaredDependencies, resourceFolder / "unit-test-project")
        .scoped { eval =>
          val Right(UnitTester.Result(UndeclaredReport(undeclared, _), _)) =
            eval(
              UndeclaredDependencies.undeclaredCompileDependenciesAnon
            ).runtimeChecked
          assert(!undeclared.exists(_.fullName.startsWith("fs2-core")))
        }
    }

    test("no unused dependencies in clean module") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(NoDependencyIssues, resourceFolder / "unit-test-project")
        .scoped { eval =>
          val Right(UnitTester.Result(UnusedReport(unused, _), _)) =
            eval(
              NoDependencyIssues.unusedCompileDependenciesAnon
            ).runtimeChecked
          assert(unused.isEmpty)
        }
    }

    test("no undeclared dependencies in clean module") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(NoDependencyIssues, resourceFolder / "unit-test-project")
        .scoped { eval =>
          val Right(UnitTester.Result(UndeclaredReport(undeclared, _), _)) =
            eval(
              NoDependencyIssues.undeclaredCompileDependenciesAnon
            ).runtimeChecked
          assert(undeclared.isEmpty)
        }
    }

    test("undeclared filter suppresses filtered dependency") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(
        FilteredUndeclaredDependencies,
        resourceFolder / "unit-test-project"
      ).scoped { eval =>
        val Right(UnitTester.Result(UndeclaredReport(undeclared, _), _)) =
          eval(
            FilteredUndeclaredDependencies.undeclaredCompileDependenciesAnon
          ).runtimeChecked
        assert(!undeclared.exists(_.fullName.startsWith("cats-effect")))
      }
    }

    test("unused filter suppresses filtered dependency") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(
        FilteredUnusedDependencies,
        resourceFolder / "unit-test-project"
      ).scoped { eval =>
        val Right(UnitTester.Result(UnusedReport(unused, _), _)) =
          eval(
            FilteredUnusedDependencies.unusedCompileDependenciesAnon
          ).runtimeChecked
        assert(!unused.exists(_.fullName.startsWith("fs2-core")))
      }
    }

    test("scalajs-library should not be reported as undeclared") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(ScalaJSProject, resourceFolder / "unit-test-project")
        .scoped { eval =>
          val Right(UnitTester.Result(UndeclaredReport(undeclared, _), _)) =
            eval(
              ScalaJSProject.undeclaredCompileDependenciesAnon
            ).runtimeChecked

          assert(!undeclared.exists(_.presentation.contains("scalajs-library")))
        }
    }

    test("scalajs undeclared dependency is detected") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(ScalaJSProject, resourceFolder / "unit-test-project")
        .scoped { eval =>
          val Right(UnitTester.Result(UndeclaredReport(undeclared, _), _)) =
            eval(
              ScalaJSProject.undeclaredCompileDependenciesAnon
            ).runtimeChecked
          assert(undeclared.exists(_.fullName.startsWith("cats-effect")))
        }
    }

    test("scalajs unused dependency is detected") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(ScalaJSProject, resourceFolder / "unit-test-project")
        .scoped { eval =>
          val Right(UnitTester.Result(UnusedReport(unused, _), _)) =
            eval(
              ScalaJSProject.unusedCompileDependenciesAnon
            ).runtimeChecked
          assert(!unused.exists(_.fullName.startsWith("cats-effect")))
          assert(unused.exists(_.fullName.startsWith("fs2-core")))
        }
    }

    test("scalanative undeclared dependency is detected") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(ScalaNativeProject, resourceFolder / "unit-test-project")
        .scoped { eval =>
          val Right(UnitTester.Result(UndeclaredReport(undeclared, _), _)) =
            eval(
              ScalaNativeProject.undeclaredCompileDependenciesAnon
            ).runtimeChecked
          assert(undeclared.exists(_.fullName.startsWith("cats-effect")))
        }
    }

    test("scalanative unused dependency is detected") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(ScalaNativeProject, resourceFolder / "unit-test-project")
        .scoped { eval =>
          val Right(UnitTester.Result(UnusedReport(unused, _), _)) =
            eval(
              ScalaNativeProject.unusedCompileDependenciesAnon
            ).runtimeChecked
          assert(!unused.exists(_.fullName.startsWith("cats-effect")))
          assert(unused.exists(_.fullName.startsWith("fs2-core")))
        }
    }

    test("modules - unused module dep is detected") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(UnusedModuleDeps, resourceFolder / "module-deps-unused")
        .scoped { eval =>
          val Right(UnitTester.Result(UnusedReport(_, unused), _)) =
            eval(
              UnusedModuleDeps.app.unusedCompileDependenciesAnon
            ).runtimeChecked
          assert(
            unused.exists(d => d.contains("core"))
          )
        }
    }

    test("modules - undeclared transitive module dep is detected") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(
        UndeclaredModuleDeps,
        resourceFolder / "module-deps-undeclared"
      ).scoped { eval =>
        val Right(UnitTester.Result(UndeclaredReport(_, undeclared), _)) =
          eval(
            UndeclaredModuleDeps.app.undeclaredCompileDependenciesAnon
          ).runtimeChecked
        // app uses core.CoreModel directly but only declares middle in moduleDeps
        assert(undeclared.contains("core"))
      }
    }

    test("modules - declared module dep is not undeclared") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(
        UndeclaredModuleDeps,
        resourceFolder / "module-deps-undeclared"
      ).scoped { eval =>
        val Right(UnitTester.Result(UndeclaredReport(_, undeclared), _)) =
          eval(
            UndeclaredModuleDeps.app.undeclaredCompileDependenciesAnon
          ).runtimeChecked
        assert(!undeclared.exists(d => d.contains("middle")))
      }
    }

    test("modules - no unused module deps in clean project") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(CleanModuleDeps, resourceFolder / "module-deps-clean")
        .scoped { eval =>
          val Right(UnitTester.Result(UnusedReport(_, unused), _)) =
            eval(
              CleanModuleDeps.app.unusedCompileDependenciesAnon
            ).runtimeChecked
          // app properly uses core, so core should not be reported as unused
          assert(!unused.exists(d => d.contains("core")))
        }
    }

    test("modules - no undeclared module deps in clean project") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(CleanModuleDeps, resourceFolder / "module-deps-clean")
        .scoped { eval =>
          val Right(UnitTester.Result(UndeclaredReport(_, undeclared), _)) =
            eval(
              CleanModuleDeps.app.undeclaredCompileDependenciesAnon
            ).runtimeChecked
          assert(undeclared.isEmpty)
        }
    }

    test("modules - undeclared nested transitive module dep is detected") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(
        NestedUndeclaredModuleDeps,
        resourceFolder / "module-deps-nested-undeclared"
      ).scoped { eval =>
        val Right(UnitTester.Result(UndeclaredReport(_, undeclared), _)) =
          eval(
            NestedUndeclaredModuleDeps.app.undeclaredCompileDependenciesAnon
          ).runtimeChecked
        // app uses core.CoreModel directly but only declares outer.middle in moduleDeps
        assert(
          undeclared.exists(d => d.contains("core"))
        )
      }
    }

    test("modules - declared nested module dep is not undeclared") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(
        NestedUndeclaredModuleDeps,
        resourceFolder / "module-deps-nested-undeclared"
      ).scoped { eval =>
        val Right(UnitTester.Result(UndeclaredReport(_, undeclared), _)) =
          eval(
            NestedUndeclaredModuleDeps.app.undeclaredCompileDependenciesAnon
          ).runtimeChecked
        assert(!undeclared.exists(d => d.contains("middle")))
      }
    }

    test("modules - undeclared module filter suppresses filtered module") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(
        FilteredUndeclaredModuleDeps,
        resourceFolder / "module-deps-undeclared"
      ).scoped { eval =>
        val Right(UnitTester.Result(UndeclaredReport(_, undeclared), _)) =
          eval(
            FilteredUndeclaredModuleDeps.app.undeclaredCompileDependenciesAnon
          ).runtimeChecked
        assert(!undeclared.exists(d => d.contains("core")))
      }
    }

    test("modules - unused module filter suppresses filtered module") {
      val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
      UnitTester(
        FilteredUnusedModuleDeps,
        resourceFolder / "module-deps-unused"
      ).scoped { eval =>
        val Right(UnitTester.Result(UnusedReport(_, unused), _)) =
          eval(
            FilteredUnusedModuleDeps.app.unusedCompileDependenciesAnon
          ).runtimeChecked
        assert(!unused.exists(d => d.contains("core")))
      }
    }

  }
}
