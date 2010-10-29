class ShiroUser {

	String username
	String passwordHash
	String name

	static hasMany = [roles: ShiroRole, permissions: String]

	static constraints = {
		username nullable: false, blank: false
	}
}
