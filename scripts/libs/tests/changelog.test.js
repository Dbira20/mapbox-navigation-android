const mockfs = require('mock-fs');
const assert = require('assert');
const fs = require('fs')
const path = require('path')


const testingDataDir = path.join(__dirname, 'testingData')

const { addReleaseNotesToChangelogMD, compileReleaseNotesMd, createEntry, isValidEntry } = require('../changelog')

describe('addReleaseNotesToChangelogMD', function () {
  it('adding new release notes to existing changelog file', function () {
    let existingFileContent = fs.readFileSync(path.join(testingDataDir, 'existing-changelog.md')).toString()
    let result = addReleaseNotesToChangelogMD(existingFileContent, "TEST_RELEASE_TEXT")
    let expectedChangelogContent = fs.readFileSync(path.join(testingDataDir, 'existing-changelog-with-new-release.md')).toString()
    assert.equal(result, expectedChangelogContent)
  })
});

describe('compile changelog', function () {
  it('compile changelog for issues of each type', async function () {
    let expectedResult = fs.readFileSync(path.join(testingDataDir, "expected-all-issues-types-changelog.md")).toString()
    await mockFileSystem(async function () {
      await createEntry(
        {
          ticket: 1,
          type: 'added',
          title: 'Test ticket 1'
        },
        "branch1"
      )
      await createEntry(
        {
          ticket: 2,
          type: 'fixed',
          title: 'Test ticket 2'
        },
        "branch1")
      await createEntry(
        {
          ticket: 3,
          type: 'added',
          title: 'Test ticket 3'
        },
        "branch3")
      await createEntry(
        {
          ticket: 4,
          type: 'fixed',
          title: 'Test ticket 4'
        },
        "branch4")
      let changelog = compileReleaseNotesMd({
        version: "2.1.0",
        dependenciesMd: "TEST_DEPENDENCIES",
        releaseDate: new Date(2022, 0, 9),
        fileCreationDataProvider: function (path) {
          return path.substring(path.length - 1)
        }
      })
      assert.equal(changelog, expectedResult)
    })
  })

  it('display patch warning for 2.0.1', async function () {
    await mockFileSystem(async function () {
      await createEntry(
        {
          ticket: 1,
          type: 'fixed',
          title: 'test ticket'
        },
        "test"
      )

      let changelog = compileReleaseNotesMd({
        version: "2.0.4",
        dependenciesMd: "TEST_DEPENDENCIES",
        releaseDate: new Date(2022, 0, 9),
        fileCreationDataProvider: function (path) {
          return path.substring(path.length - 1)
        }
      })

      if (!changelog.includes("This is a patch release on top of v2.0.x")) {
        assert.fail(`changelog doesn't contain message for 2.0.x release.\n ${changelog}`)
      }
    })
  })

  it('few release entries for one PR', async function () {
    await mockFileSystem(async function () {
      await createEntry(
        {
          ticket: 7,
          type: 'fixed',
          title: 'test fix'
        },
        "test"
      )
      await createEntry(
        {
          ticket: 7,
          type: 'added',
          title: 'test feature'
        },
        "test"
      )

      let changelog = compileReleaseNotesMd({
        version: "2.1.8",
        dependenciesMd: "TEST_DEPENDENCIES",
        releaseDate: new Date(2022, 0, 9),
        fileCreationDataProvider: function (path) {
          return path.substring(path.length - 1)
        }
      })

      if (!changelog.includes("test feature")) {
        assert.fail(`changelog doesn't contain message for test feature entry:\n${changelog}`)
      }
      if (!changelog.includes("test fix")) {
        assert.fail(`changelog doesn't contain message for test fix entry:\n${changelog}`)
      }
    })
  })
})

describe("validate entry", function () {
  it("fixed entry", function () {
    let result = isValidEntry([{ ticket: 1, title: "test", type: "fixed" }])
    assert.equal(result, true)
  })
  it("added entry", function () {
    let result = isValidEntry([{ ticket: 4, title: "test", type: "added" }])
    assert.equal(result, true)
  })
  it("changed and added entries", function () {
    let result = isValidEntry([{ ticket: 4, title: "test", type: "added" }, { ticket: 1, title: "test", type: "changed" }])
    assert.equal(result, false)
  })
  it("not array entry", function () {
    let result = isValidEntry({ ticket: 4, title: "test", type: "added" })
    assert.equal(result, false)
  })
})

async function mockFileSystem(block) {
  try {
    mockfs({})
    await block()
  } catch (error) {
    mockfs.restore()
    throw (error)
  }
}